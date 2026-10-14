package ar.edu.utn.consorcios.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import ar.edu.utn.consorcios.dto.LoginRequest;
import ar.edu.utn.consorcios.dto.LoginResponse;
import ar.edu.utn.consorcios.entity.Usuario;
import ar.edu.utn.consorcios.repository.UsuarioRepository;
import ar.edu.utn.consorcios.security.JwtService;

@Service
public class AuthService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /** Hash de relleno para que el tiempo de respuesta no revele si el email existe. */
    private final String hashRelleno;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.hashRelleno = passwordEncoder.encode("relleno");
    }

    public LoginResponse login(LoginRequest request) {
        var usuario = usuarios.findByEmailIgnoreCase(request.email().trim()).filter(Usuario::isActivo);
        String hash = usuario.map(Usuario::getPasswordHash).orElse(hashRelleno);
        boolean coincide = passwordEncoder.matches(request.password(), hash);
        if (usuario.isEmpty() || !coincide) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }
        Usuario u = usuario.get();
        return new LoginResponse(jwtService.generar(u.getEmail(), u.getRol()), u.getEmail(), u.getRol());
    }
}
