package ar.edu.utn.consorcios.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record UnidadesRequest(@NotNull @Valid List<UnidadRequest> unidades) {
}
