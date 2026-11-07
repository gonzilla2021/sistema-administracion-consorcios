package ar.edu.utn.consorcios.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import ar.edu.utn.consorcios.entity.Consorcio;
import ar.edu.utn.consorcios.entity.Rol;
import ar.edu.utn.consorcios.entity.Usuario;

public record UsuarioResponse(UUID id, String email, Rol rol, boolean activo, LocalDateTime fechaAlta,
        UUID personaId, List<UUID> consorcioIds) {

    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getEmail(), u.getRol(), u.isActivo(), u.getFechaAlta(),
                u.getPersona() == null ? null : u.getPersona().getId(),
                u.getConsorcios().stream().map(Consorcio::getId).sorted().toList());
    }
}
