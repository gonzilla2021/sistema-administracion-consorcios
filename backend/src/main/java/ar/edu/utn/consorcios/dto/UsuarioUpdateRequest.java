package ar.edu.utn.consorcios.dto;

import java.util.List;
import java.util.UUID;

/** consorcioIds: si viene, reemplaza los consorcios del administrador; si es null, no se modifican. */
public record UsuarioUpdateRequest(Boolean activo, List<UUID> consorcioIds) {
}
