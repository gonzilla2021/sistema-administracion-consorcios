package ar.edu.utn.consorcios.service;
import ar.edu.utn.consorcios.dto.GastoRequest;
import ar.edu.utn.consorcios.entity.*;
import ar.edu.utn.consorcios.repository.*;
import org.junit.jupiter.api.*;
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
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class GastoServiceTest {
 @Mock GastoRepository gastos;
 @Mock LiquidacionRepository liquidaciones;
 @Mock ConsorcioService acceso;
 @Mock Authentication auth;
 @InjectMocks GastoService service;
 final UUID cid=UUID.randomUUID(),gid=UUID.randomUUID();
 final LocalDate fecha=LocalDate.of(2026,2,10);
 Consorcio consorcio;
 Gasto gasto;
 @BeforeEach void preparar() {
  consorcio=new Consorcio(); consorcio.setId(cid);
  gasto=new Gasto(); gasto.setId(gid); gasto.setConsorcio(consorcio);
  gasto.setFecha(fecha); gasto.setMonto(new BigDecimal("100.00"));
  gasto.setDescripcion("Original");
 }
 GastoRequest request(LocalDate dia) {
  return new GastoRequest(" Limpieza ",new BigDecimal("150.50"),TipoGasto.ORDINARIO,dia,null);
 }
 void existe() { when(gastos.findById(gid)).thenReturn(Optional.of(gasto)); }
 void guarda() { when(gastos.save(any())).thenAnswer(i->i.getArgument(0)); }
 void cerrado(String periodo) {
  Liquidacion l=new Liquidacion(); l.setEstado(EstadoLiquidacion.CERRADA);
  doReturn(Optional.of(l)).when(liquidaciones).findByConsorcioIdAndPeriodo(cid,periodo);
 }
 void rechaza(HttpStatus estado,Runnable accion) {
  var error=assertThrows(ResponseStatusException.class,accion::run);
  assertEquals(estado,error.getStatusCode());
  verify(gastos,never()).save(any()); verify(gastos,never()).delete(any());
 }
 @Test void creaGastoEnConsorcioConImporteYDescripcionCorrectos() {
  when(acceso.exigirAcceso(auth,cid)).thenReturn(consorcio); guarda();
  var respuesta=service.crear(auth,cid,request(fecha));
  assertEquals(cid,respuesta.consorcioId());
  assertEquals("Limpieza",respuesta.descripcion());
  assertEquals(new BigDecimal("150.50"),respuesta.monto());
  assertEquals(TipoGasto.ORDINARIO,respuesta.tipo());
  assertEquals(fecha,respuesta.fecha());
 }
 @Test void rechazaAltaEnPeriodoCerrado() {
  cerrado("2026-02");
  rechaza(HttpStatus.CONFLICT,()->service.crear(auth,cid,request(fecha)));
 }
 @Test void actualizaGastoEnPeriodoEditable() {
  existe(); guarda();
  service.actualizar(auth,cid,gid,request(fecha.plusDays(1)));
  assertEquals("Limpieza",gasto.getDescripcion());
  assertEquals(new BigDecimal("150.50"),gasto.getMonto());
  assertEquals(fecha.plusDays(1),gasto.getFecha());
  verify(gastos).save(gasto);
 }
 @Test void impideMoverGastoDesdePeriodoCerrado() {
  existe(); cerrado("2026-02");
  rechaza(HttpStatus.CONFLICT,()->service.actualizar(auth,cid,gid,request(fecha.plusMonths(1))));
  assertEquals(fecha,gasto.getFecha());
 }
 @Test void impideMoverGastoHaciaPeriodoCerrado() {
  existe();
  doReturn(Optional.empty()).when(liquidaciones).findByConsorcioIdAndPeriodo(cid,"2026-02");
  cerrado("2026-03");
  rechaza(HttpStatus.CONFLICT,()->service.actualizar(auth,cid,gid,request(fecha.plusMonths(1))));
  assertEquals(fecha,gasto.getFecha());
 }
 @Test void eliminaGastoDePeriodoEditable() {
  existe(); service.eliminar(auth,cid,gid); verify(gastos).delete(gasto);
 }
 @Test void impideEliminarGastoDePeriodoCerrado() {
  existe(); cerrado("2026-02");
  rechaza(HttpStatus.CONFLICT,()->service.eliminar(auth,cid,gid));
 }
 @Test void impideModificarGastoDeOtroConsorcio() {
  existe(); Consorcio otro=new Consorcio(); otro.setId(UUID.randomUUID()); gasto.setConsorcio(otro);
  rechaza(HttpStatus.NOT_FOUND,()->service.actualizar(auth,cid,gid,request(fecha)));
  verifyNoInteractions(liquidaciones);
 }
}

