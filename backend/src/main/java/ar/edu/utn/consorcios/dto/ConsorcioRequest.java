package ar.edu.utn.consorcios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConsorcioRequest(
        @NotBlank @Size(max = 150) String nombre,
        @NotBlank @Size(max = 250) String direccion) {
}
