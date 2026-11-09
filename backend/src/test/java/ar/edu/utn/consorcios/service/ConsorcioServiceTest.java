package ar.edu.utn.consorcios.service;
import ar.edu.utn.consorcios.entity.Consorcio;
import ar.edu.utn.consorcios.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class ConsorcioServiceTest {
 @Mock ConsorcioRepository consorcios;
 @Mock UnidadFuncionalRepository unidades;
 @InjectMocks ConsorcioService service;
 final UUID cid=UUID.randomUUID();
 Authentication usuario(String rol) {
  return new UsernamePasswordAuthenticationToken("usuario@example.test","",List.of(new SimpleGrantedAuthority("ROLE_"+rol)));
 }
 @Test void superadministradorAccedeACualquierConsorcioExistente() {
  Consorcio c=new Consorcio(); c.setId(cid);
  when(consorcios.findById(cid)).thenReturn(Optional.of(c));
  assertSame(c,service.exigirAcceso(usuario("SUPERADMINISTRADOR"),cid));
 }
 @Test void superadministradorRecibe404SiNoExiste() {
  when(consorcios.findById(cid)).thenReturn(Optional.empty());
  var error=assertThrows(ResponseStatusException.class,()->service.exigirAcceso(usuario("SUPERADMINISTRADOR"),cid));
  assertEquals(HttpStatus.NOT_FOUND,error.getStatusCode());
 }
 @Test void administradorAccedeAConsorcioAsignado() {
  Consorcio c=new Consorcio(); c.setId(cid);
  when(consorcios.findAdministradoPor("usuario@example.test",cid)).thenReturn(Optional.of(c));
  assertSame(c,service.exigirAcceso(usuario("ADMINISTRADOR_CONSORCIO"),cid));
  verify(consorcios,never()).findById(cid);
 }
 @Test void administradorNoAccedeAConsorcioAjeno() {
  when(consorcios.findAdministradoPor("usuario@example.test",cid)).thenReturn(Optional.empty());
  var error=assertThrows(ResponseStatusException.class,()->service.exigirAcceso(usuario("ADMINISTRADOR_CONSORCIO"),cid));
  assertEquals(HttpStatus.FORBIDDEN,error.getStatusCode());
 }
 @ParameterizedTest @ValueSource(strings={"PROPIETARIO","INQUILINO"})
 void residentesNoPuedenAdministrarConsorcios(String rol) {
  var error=assertThrows(ResponseStatusException.class,()->service.exigirAcceso(usuario(rol),cid));
  assertEquals(HttpStatus.FORBIDDEN,error.getStatusCode());
  verifyNoInteractions(consorcios,unidades);
 }
}
