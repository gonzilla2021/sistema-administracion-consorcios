package ar.edu.utn.consorcios.dto;

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
        /** Obligatorio para ADMINISTRADOR_CONSORCIO; no admitido para el resto. */
        UUID consorcioId) {
}
