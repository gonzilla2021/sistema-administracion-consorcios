package ar.edu.utn.consorcios.service;
import ar.edu.utn.consorcios.controller.PagoController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Usa el proxy real de seguridad de Spring, sin levantar JPA ni conectar a la base.
@SpringJUnitConfig(PagoPermisosTest.Config.class)
class PagoPermisosTest {
 @Configuration @EnableMethodSecurity
 static class Config {
  @Bean PagoService pagoService() { return mock(PagoService.class); }
  @Bean PagoController pagoController(PagoService service) { return new PagoController(service); }
 }
 @Autowired PagoController controller;
 @Autowired PagoService service;
 @BeforeEach void limpiar() { reset(service); }
 @Test @WithMockUser(roles="SUPERADMINISTRADOR")
 void permiteSuperadministrador() {
  UUID cid=UUID.randomUUID(); controller.listar(SecurityContextHolder.getContext().getAuthentication(),cid);
  verify(service).listar(SecurityContextHolder.getContext().getAuthentication(),cid);
 }
 @Test @WithMockUser(roles="ADMINISTRADOR_CONSORCIO")
 void permiteAdministrador() {
  UUID cid=UUID.randomUUID(); controller.listar(SecurityContextHolder.getContext().getAuthentication(),cid);
  verify(service).listar(SecurityContextHolder.getContext().getAuthentication(),cid);
 }
 @Test @WithMockUser(roles="PROPIETARIO")
 void rechazaPropietario() { rechaza(); }
 @Test @WithMockUser(roles="INQUILINO")
 void rechazaInquilino() { rechaza(); }
 void rechaza() {
  assertThrows(AccessDeniedException.class,()->controller.listar(SecurityContextHolder.getContext().getAuthentication(),UUID.randomUUID()));
  verifyNoInteractions(service);
 }
}
