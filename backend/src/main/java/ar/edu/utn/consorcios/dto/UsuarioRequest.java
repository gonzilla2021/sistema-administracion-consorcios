package ar.edu.utn.consorcios.dto;

import java.util.List;
import java.util.UUID;

import ar.edu.utn.consorcios.entity.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotNull Rol rol,
        UUID personaId,
        /** Solo para ADMINISTRADOR_CONSORCIO (puede quedar vacío); no admitido para el resto. */
        List<UUID> consorcioIds) {
}
