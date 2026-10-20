package ar.edu.utn.consorcios.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import ar.edu.utn.consorcios.entity.Rol;
import ar.edu.utn.consorcios.entity.Usuario;

public record UsuarioResponse(UUID id, String email, Rol rol, boolean activo, LocalDateTime fechaAlta,
        UUID personaId, UUID consorcioId) {

    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getEmail(), u.getRol(), u.isActivo(), u.getFechaAlta(),
                u.getPersona() == null ? null : u.getPersona().getId(),
                u.getConsorcio() == null ? null : u.getConsorcio().getId());
    }
}
