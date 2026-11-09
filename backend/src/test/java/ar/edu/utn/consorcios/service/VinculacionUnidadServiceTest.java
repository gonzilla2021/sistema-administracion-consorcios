package ar.edu.utn.consorcios.service;
import ar.edu.utn.consorcios.dto.VinculacionRequest;
import ar.edu.utn.consorcios.entity.*;
import ar.edu.utn.consorcios.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
@ExtendWith(MockitoExtension.class)
class VinculacionUnidadServiceTest {
 @Mock VinculacionUnidadRepository vinculaciones;
 @Mock UnidadFuncionalRepository unidades;
 @Mock PersonaRepository personas;
 @Mock ConsorcioService acceso;
 @Mock Authentication auth;
 @InjectMocks VinculacionUnidadService service;
 final UUID cid=UUID.randomUUID(),uid=UUID.randomUUID(),pid=UUID.randomUUID();
 Persona persona; UnidadFuncional unidad;
 final LocalDate inicio=LocalDate.of(2026,1,10),fin=LocalDate.of(2026,1,20);
 @BeforeEach void preparar() {
  Consorcio c=new Consorcio(); c.setId(cid);
  unidad=new UnidadFuncional(); unidad.setId(uid); unidad.setConsorcio(c);
  persona=new Persona(); persona.setId(pid); persona.setConsorcio(c);
  when(unidades.findById(uid)).thenReturn(Optional.of(unidad));
 }
 VinculacionRequest request(LocalDate desde,LocalDate hasta) {
  return new VinculacionRequest(pid,TipoVinculo.PROPIETARIO,desde,hasta);
 }
 void rechaza(HttpStatus estado,Runnable accion) {
  var error=assertThrows(ResponseStatusException.class,accion::run);
  assertEquals(estado,error.getStatusCode()); verify(vinculaciones,never()).save(any());
 }
 @Test void rechazaFechaFinAnteriorAlInicio() {
  rechaza(HttpStatus.BAD_REQUEST,()->service.crear(auth,cid,uid,request(fin,inicio)));
  verifyNoInteractions(personas);
 }
 @Test void rechazaPersonaDeOtroConsorcio() {
  Consorcio otro=new Consorcio(); otro.setId(UUID.randomUUID()); persona.setConsorcio(otro);
  when(personas.findById(pid)).thenReturn(Optional.of(persona));
  rechaza(HttpStatus.CONFLICT,()->service.crear(auth,cid,uid,request(inicio,fin)));
 }
 @ParameterizedTest
 @CsvSource({"2026-01-01,2026-01-10","2026-01-20,2026-01-30","2026-01-12,2026-01-15","2026-01-01,2026-01-30"})
 void rechazaPeriodosSuperpuestosIncluidosLosExtremos(String desde,String hasta) {
  when(personas.findById(pid)).thenReturn(Optional.of(persona));
  VinculacionUnidad anterior=new VinculacionUnidad(); anterior.setPersona(persona);
  anterior.setTipo(TipoVinculo.PROPIETARIO); anterior.setFechaInicio(inicio); anterior.setFechaFin(fin);
  when(vinculaciones.findByUnidadFuncionalId(uid)).thenReturn(List.of(anterior));
  rechaza(HttpStatus.CONFLICT,()->service.crear(auth,cid,uid,request(LocalDate.parse(desde),LocalDate.parse(hasta))));
 }
 @Test void permitePeriodosSeparados() {
  when(personas.findById(pid)).thenReturn(Optional.of(persona));
  VinculacionUnidad anterior=new VinculacionUnidad(); anterior.setPersona(persona);
  anterior.setTipo(TipoVinculo.PROPIETARIO); anterior.setFechaInicio(inicio); anterior.setFechaFin(fin);
  when(vinculaciones.findByUnidadFuncionalId(uid)).thenReturn(List.of(anterior));
  when(vinculaciones.save(any())).thenAnswer(i->i.getArgument(0));
  service.crear(auth,cid,uid,request(fin.plusDays(1),null));
  verify(vinculaciones).save(any());
 }
 @Test void rechazaUnidadDeOtroConsorcio() {
  unidad.getConsorcio().setId(UUID.randomUUID());
  rechaza(HttpStatus.NOT_FOUND,()->service.crear(auth,cid,uid,request(inicio,fin)));
  verifyNoInteractions(personas);
 }
}
