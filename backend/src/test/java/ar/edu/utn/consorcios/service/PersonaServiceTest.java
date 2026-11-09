package ar.edu.utn.consorcios.service;
import ar.edu.utn.consorcios.dto.PersonaRequest;
import ar.edu.utn.consorcios.entity.*;
import ar.edu.utn.consorcios.repository.PersonaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
@ExtendWith(MockitoExtension.class)
class PersonaServiceTest {
 @Mock PersonaRepository personas;
 @Mock ConsorcioService acceso;
 @Mock Authentication auth;
 @InjectMocks PersonaService service;
 final UUID cid=UUID.randomUUID();
 final PersonaRequest request=new PersonaRequest(" Ana "," Perez "," 12345678 ","ana@example.test",null);
 @Test void creaPersonaEnConsorcioYNormalizaDatos() {
  Consorcio c=new Consorcio(); c.setId(cid);
  when(acceso.exigirAcceso(auth,cid)).thenReturn(c);
  when(personas.save(any())).thenAnswer(i->i.getArgument(0));
  var respuesta=service.crear(auth,cid,request);
  assertEquals("Ana",respuesta.nombre()); assertEquals("Perez",respuesta.apellido());
  assertEquals("12345678",respuesta.dni());
  ArgumentCaptor<Persona> captor=ArgumentCaptor.forClass(Persona.class);
  verify(personas).save(captor.capture()); assertSame(c,captor.getValue().getConsorcio());
 }
 @Test void rechazaDniDuplicadoDentroDelConsorcio() {
  when(personas.existsByConsorcioIdAndDni(cid,"12345678")).thenReturn(true);
  var error=assertThrows(ResponseStatusException.class,()->service.crear(auth,cid,request));
  assertEquals(HttpStatus.CONFLICT,error.getStatusCode()); verify(personas,never()).save(any());
 }
 @Test void impideConsultarPersonaDeOtroConsorcio() {
  UUID pid=UUID.randomUUID(); Consorcio otro=new Consorcio(); otro.setId(UUID.randomUUID());
  Persona p=new Persona(); p.setConsorcio(otro);
  when(personas.findById(pid)).thenReturn(Optional.of(p));
  var error=assertThrows(ResponseStatusException.class,()->service.obtener(auth,cid,pid));
  assertEquals(HttpStatus.NOT_FOUND,error.getStatusCode());
 }
}
