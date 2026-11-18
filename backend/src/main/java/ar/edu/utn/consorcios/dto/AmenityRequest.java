package ar.edu.utn.consorcios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AmenityRequest(

    @NotBlank(message = "El nombre es obligatorio")
    @Size(
        max = 100,
        message = "El nombre no puede superar los 100 caracteres"
    )
    String nombre,

    @Size(
        max = 500,
        message = "La descripción no puede superar los 500 caracteres"
    )
    String descripcion,

    boolean activo
) {
}