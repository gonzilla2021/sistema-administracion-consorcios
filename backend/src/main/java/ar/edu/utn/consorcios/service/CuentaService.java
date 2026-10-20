package ar.edu.utn.consorcios.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ar.edu.utn.consorcios.dto.CambioPasswordRequest;
import ar.edu.utn.consorcios.entity.Usuario;
import ar.edu.utn.consorcios.repository.UsuarioRepository;

@Service
public class CuentaService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;

    public CuentaService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void cambiarPassword(String email, CambioPasswordRequest request) {
        Usuario u = usuarios.findByEmailIgnoreCase(email).filter(Usuario::isActivo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión inválida"));
        if (!passwordEncoder.matches(request.passwordActual(), u.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña actual no es correcta");
        }
        if (request.passwordActual().equals(request.passwordNueva())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La nueva contraseña debe ser distinta de la actual");
        }
        u.setPasswordHash(passwordEncoder.encode(request.passwordNueva()));
    }
}
