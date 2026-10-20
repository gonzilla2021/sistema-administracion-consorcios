package ar.edu.utn.consorcios.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ar.edu.utn.consorcios.dto.UsuarioRequest;
import ar.edu.utn.consorcios.dto.UsuarioResponse;
import ar.edu.utn.consorcios.dto.UsuarioUpdateRequest;
import ar.edu.utn.consorcios.entity.Rol;
import ar.edu.utn.consorcios.entity.Usuario;
import ar.edu.utn.consorcios.repository.ConsorcioRepository;
import ar.edu.utn.consorcios.repository.PersonaRepository;
import ar.edu.utn.consorcios.repository.UsuarioRepository;

/** Gestión de cuentas. Solo la usa el superadministrador (se exige en el controlador). */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final ConsorcioRepository consorcios;
    private final PersonaRepository personas;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarios, ConsorcioRepository consorcios, PersonaRepository personas,
            PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.consorcios = consorcios;
        this.personas = personas;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarios.findAll().stream().map(UsuarioResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(UUID id) {
        return UsuarioResponse.de(buscar(id));
    }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        String email = request.email().trim();
        if (usuarios.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese email");
        }
        Usuario u = new Usuario();
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(request.password()));
        u.setRol(request.rol());
        if (request.personaId() != null) {
            if (usuarios.existsByPersonaId(request.personaId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "La persona ya tiene un usuario");
            }
            u.setPersona(personas.findById(request.personaId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Persona inexistente")));
        }
        asignarConsorcio(u, request.consorcioId());
        return UsuarioResponse.de(usuarios.save(u));
    }

    @Transactional
    public UsuarioResponse actualizar(Authentication auth, UUID id, UsuarioUpdateRequest request) {
        Usuario u = buscar(id);
        if (request.activo() != null) {
            if (!request.activo() && u.getEmail().equalsIgnoreCase(auth.getName())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No podés desactivar tu propia cuenta");
            }
            u.setActivo(request.activo());
        }
        if (request.consorcioId() != null) {
            asignarConsorcio(u, request.consorcioId());
        }
        return UsuarioResponse.de(u);
    }

    private void asignarConsorcio(Usuario u, UUID consorcioId) {
        if (u.getRol() != Rol.ADMINISTRADOR_CONSORCIO) {
            if (consorcioId != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Solo el administrador de consorcio lleva consorcio asignado");
            }
            return;
        }
        if (consorcioId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El administrador de consorcio requiere consorcioId");
        }
        u.setConsorcio(consorcios.findById(consorcioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Consorcio inexistente")));
    }

    private Usuario buscar(UUID id) {
        return usuarios.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario inexistente"));
    }
}
