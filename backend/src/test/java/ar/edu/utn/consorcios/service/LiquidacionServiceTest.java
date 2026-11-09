package ar.edu.utn.consorcios.service;

import ar.edu.utn.consorcios.dto.LiquidacionRequest;
import ar.edu.utn.consorcios.entity.*;
import ar.edu.utn.consorcios.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LiquidacionServiceTest {
    @Mock LiquidacionRepository liquidaciones;
    @Mock LiquidacionUnidadRepository detalles;
    @Mock UnidadFuncionalRepository unidades;
    @Mock GastoRepository gastos;
    @Mock ConsorcioService acceso;
    @Mock Authentication auth;
    @InjectMocks LiquidacionService service;
    final UUID consorcioId = UUID.randomUUID();
    final LiquidacionRequest request = new LiquidacionRequest("2026-02");
    Consorcio consorcio;

    @BeforeEach void preparar() {
        consorcio = new Consorcio();
        consorcio.setId(consorcioId);
        when(acceso.exigirAcceso(auth, consorcioId)).thenReturn(consorcio);
    }
    void gastos(String monto) {
        Gasto gasto = new Gasto();
        gasto.setMonto(new BigDecimal(monto));
        when(gastos.findByConsorcioIdAndFechaBetween(consorcioId,
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28))).thenReturn(List.of(gasto));
    }
    UnidadFuncional unidad(int numero, String coeficiente) {
        UnidadFuncional u = new UnidadFuncional();
        u.setId(new UUID(0, numero));
        u.setConsorcio(consorcio);
        u.setCoeficientePropiedad(new BigDecimal(coeficiente));
        return u;
    }
    void guardarLiquidacion() {
        when(liquidaciones.save(any())).thenAnswer(inv -> {
            Liquidacion l = inv.getArgument(0);
            l.setId(UUID.randomUUID());
            return l;
        });
    }
    void rechazaGeneracion() {
        var error = assertThrows(ResponseStatusException.class,
                () -> service.generar(auth, consorcioId, request));
        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(liquidaciones, never()).save(any());
        verify(detalles, never()).save(any());
    }
    @Test void reparteGastosSegunCoeficientes() {
        gastos("1000.00"); guardarLiquidacion();
        when(unidades.findByConsorcioIdOrderByIdentificador(consorcioId))
                .thenReturn(List.of(unidad(1, "25"), unidad(2, "75")));
        var respuesta = service.generar(auth, consorcioId, request);
        ArgumentCaptor<LiquidacionUnidad> captor = ArgumentCaptor.forClass(LiquidacionUnidad.class);
        verify(detalles, times(2)).save(captor.capture());
        assertEquals(new BigDecimal("250.00"), captor.getAllValues().get(0).getImporte());
        assertEquals(new BigDecimal("750.00"), captor.getAllValues().get(1).getImporte());
        assertEquals(new BigDecimal("1000.00"), respuesta.totalGastos());
        assertEquals(2, respuesta.cantidadUnidades());
        assertEquals(EstadoLiquidacion.ABIERTA, respuesta.estado());
        assertTrue(captor.getAllValues().stream().allMatch(d -> d.getEstadoPago() == EstadoPago.PENDIENTE));
    }
    @Test void conservaTotalAlAjustarCentavos() {
        gastos("100.00"); guardarLiquidacion();
        consorcio.setTotalCoeficientes(new BigDecimal("3"));
        when(unidades.findByConsorcioIdOrderByIdentificador(consorcioId))
                .thenReturn(List.of(unidad(3, "1"), unidad(1, "1"), unidad(2, "1")));
        service.generar(auth, consorcioId, request);
        ArgumentCaptor<LiquidacionUnidad> captor = ArgumentCaptor.forClass(LiquidacionUnidad.class);
        verify(detalles, times(3)).save(captor.capture());
        var importes = captor.getAllValues().stream().map(LiquidacionUnidad::getImporte).toList();
        assertEquals(List.of(new BigDecimal("33.33"), new BigDecimal("33.33"), new BigDecimal("33.34")), importes);
        assertEquals(new BigDecimal("100.00"), importes.stream().reduce(BigDecimal.ZERO, BigDecimal::add));
    }
    @Test void rechazaPeriodoDuplicado() {
        when(liquidaciones.findByConsorcioIdAndPeriodo(consorcioId, "2026-02"))
                .thenReturn(Optional.of(new Liquidacion()));
        rechazaGeneracion();
        verifyNoInteractions(gastos, unidades);
    }
    @Test void rechazaPeriodoSinGastos() {
        when(gastos.findByConsorcioIdAndFechaBetween(consorcioId,
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28))).thenReturn(List.of());
        rechazaGeneracion();
    }
    @Test void rechazaConsorcioSinUnidades() {
        gastos("100.00");
        when(unidades.findByConsorcioIdOrderByIdentificador(consorcioId)).thenReturn(List.of());
        rechazaGeneracion();
    }
    @Test void rechazaCoeficientesQueNoSumanElTotal() {
        gastos("100.00");
        when(unidades.findByConsorcioIdOrderByIdentificador(consorcioId))
                .thenReturn(List.of(unidad(1, "40"), unidad(2, "40")));
        rechazaGeneracion();
    }
    @Test void rechazaTotalDeCoeficientesCero() {
        gastos("100.00"); consorcio.setTotalCoeficientes(BigDecimal.ZERO);
        when(unidades.findByConsorcioIdOrderByIdentificador(consorcioId)).thenReturn(List.of(unidad(1, "100")));
        rechazaGeneracion();
    }
}
