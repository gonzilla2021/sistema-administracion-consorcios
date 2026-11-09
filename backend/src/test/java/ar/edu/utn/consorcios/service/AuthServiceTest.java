package ar.edu.utn.consorcios.service;

import ar.edu.utn.consorcios.dto.LoginRequest;
import ar.edu.utn.consorcios.entity.Rol;
import ar.edu.utn.consorcios.entity.Usuario;
import ar.edu.utn.consorcios.repository.UsuarioRepository;
import ar.edu.utn.consorcios.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock UsuarioRepository usuarios;
    @Mock JwtService jwt;
    // BCrypt real: se verifica la contraseña contra un hash, sin usar credenciales reales.
    final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    AuthService service;
    Usuario usuario;
    final String email = "prueba@example.test";
    final String clave = "clave-ficticia-solo-para-test";

    @BeforeEach void preparar() {
        service = new AuthService(usuarios, encoder, jwt);
        usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setPasswordHash(encoder.encode(clave));
        usuario.setRol(Rol.SUPERADMINISTRADOR);
    }

    @Test void loginValidoDevuelveTokenEmailYRol() {
        when(usuarios.findByEmailIgnoreCase(email)).thenReturn(Optional.of(usuario));
        when(jwt.generar(email, Rol.SUPERADMINISTRADOR)).thenReturn("token-simulado");
        var respuesta = service.login(new LoginRequest("  " + email + "  ", clave));
        assertEquals("token-simulado", respuesta.token());
        assertEquals(email, respuesta.email());
        assertEquals(Rol.SUPERADMINISTRADOR, respuesta.rol());
        verify(jwt).generar(email, Rol.SUPERADMINISTRADOR);
    }

    @Test void rechazaContrasenaIncorrectaSinEmitirToken() {
        when(usuarios.findByEmailIgnoreCase(email)).thenReturn(Optional.of(usuario));
        rechaza(new LoginRequest(email, "otra-clave"));
    }

    @Test void rechazaUsuarioInexistenteSinEmitirToken() {
        when(usuarios.findByEmailIgnoreCase(email)).thenReturn(Optional.empty());
        rechaza(new LoginRequest(email, clave));
    }

    @Test void rechazaUsuarioInactivoAunqueLaContrasenaSeaCorrecta() {
        usuario.setActivo(false);
        when(usuarios.findByEmailIgnoreCase(email)).thenReturn(Optional.of(usuario));
        rechaza(new LoginRequest(email, clave));
    }

    private void rechaza(LoginRequest request) {
        var error = assertThrows(ResponseStatusException.class, () -> service.login(request));
        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
        assertEquals("Credenciales inválidas", error.getReason());
        verifyNoInteractions(jwt);
    }
}
