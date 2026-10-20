package ar.edu.utn.consorcios.dto;

import java.util.UUID;

public record UsuarioUpdateRequest(Boolean activo, UUID consorcioId) {
}
