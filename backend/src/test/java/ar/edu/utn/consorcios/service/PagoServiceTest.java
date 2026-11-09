package ar.edu.utn.consorcios.service;

import ar.edu.utn.consorcios.dto.PagoRequest;
import ar.edu.utn.consorcios.entity.*;
import ar.edu.utn.consorcios.repository.LiquidacionUnidadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {
    @Mock LiquidacionUnidadRepository detalles;
    @Mock ConsorcioService acceso;
    @Mock Authentication auth;
    @InjectMocks PagoService service;
    final UUID consorcioId = UUID.randomUUID();
    final UUID detalleId = UUID.randomUUID();
    LiquidacionUnidad detalle;

    @BeforeEach void preparar() {
        Consorcio consorcio = new Consorcio();
        consorcio.setId(consorcioId);
        Liquidacion liquidacion = new Liquidacion();
        liquidacion.setId(UUID.randomUUID());
        liquidacion.setEstado(EstadoLiquidacion.CERRADA);
        UnidadFuncional unidad = new UnidadFuncional();
        unidad.setId(UUID.randomUUID());
        unidad.setIdentificador("1A");
        detalle = new LiquidacionUnidad();
        detalle.setId(detalleId);
        detalle.setConsorcio(consorcio);
        detalle.setLiquidacion(liquidacion);
        detalle.setUnidadFuncional(unidad);
        detalle.setImporte(new BigDecimal("100.00"));
    }
    void existe() { when(detalles.findById(detalleId)).thenReturn(Optional.of(detalle)); }
    void guarda() { when(detalles.save(any())).thenAnswer(inv -> inv.getArgument(0)); }
    void rechaza(HttpStatus estado, Runnable accion) {
        ResponseStatusException error = assertThrows(ResponseStatusException.class, accion::run);
        assertEquals(estado, error.getStatusCode());
        verify(detalles, never()).save(any());
    }
    @Test void registraPagoDeLiquidacionCerrada() {
        existe(); guarda();
        LocalDate fecha = LocalDate.of(2026, 1, 15);
        var respuesta = service.registrar(auth, consorcioId, detalleId, new PagoRequest(fecha));
        assertEquals(EstadoPago.PAGADO, detalle.getEstadoPago());
        assertEquals(fecha, detalle.getFechaPago());
        assertEquals(EstadoPago.PAGADO, respuesta.estadoPago());
        verify(acceso).exigirAcceso(auth, consorcioId);
        verify(detalles).save(detalle);
    }
    @Test void rechazaPagoDuplicado() {
        existe(); detalle.marcarPagada(LocalDate.of(2026, 1, 15));
        rechaza(HttpStatus.CONFLICT, () -> service.registrar(auth, consorcioId, detalleId, new PagoRequest(LocalDate.now())));
    }
    @Test void rechazaPagoConLiquidacionAbierta() {
        existe(); detalle.getLiquidacion().setEstado(EstadoLiquidacion.ABIERTA);
        rechaza(HttpStatus.CONFLICT, () -> service.registrar(auth, consorcioId, detalleId, new PagoRequest(LocalDate.now())));
        assertEquals(EstadoPago.PENDIENTE, detalle.getEstadoPago());
    }
    @Test void reviertePagoYBorraFecha() {
        existe(); guarda(); detalle.marcarPagada(LocalDate.of(2026, 1, 15));
        service.revertir(auth, consorcioId, detalleId);
        assertEquals(EstadoPago.PENDIENTE, detalle.getEstadoPago());
        assertNull(detalle.getFechaPago());
        verify(detalles).save(detalle);
    }
    @Test void rechazaReversionDePagoPendiente() {
        existe();
        rechaza(HttpStatus.CONFLICT, () -> service.revertir(auth, consorcioId, detalleId));
    }
    @Test void rechazaRegistroDeOtroConsorcio() {
        existe(); detalle.getConsorcio().setId(UUID.randomUUID());
        rechaza(HttpStatus.NOT_FOUND, () -> service.registrar(auth, consorcioId, detalleId, new PagoRequest(LocalDate.now())));
        assertEquals(EstadoPago.PENDIENTE, detalle.getEstadoPago());
    }
    @Test void rechazaRegistroInexistente() {
        when(detalles.findById(detalleId)).thenReturn(Optional.empty());
        rechaza(HttpStatus.NOT_FOUND, () -> service.revertir(auth, consorcioId, detalleId));
    }
    @Test void detieneOperacionSiSeDeniegaAcceso() {
        when(acceso.exigirAcceso(auth, consorcioId)).thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN));
        rechaza(HttpStatus.FORBIDDEN, () -> service.registrar(auth, consorcioId, detalleId, new PagoRequest(LocalDate.now())));
        verify(detalles, never()).findById(any());
    }
}
