package ar.edu.utn.consorcios.dto;

import ar.edu.utn.consorcios.entity.Amenity;

import java.util.UUID;

public record AmenityResponse(
    UUID id,
    UUID consorcioId,
    String nombre,
    String descripcion,
    boolean activo
) {

    public static AmenityResponse desde(Amenity amenity) {
        return new AmenityResponse(
            amenity.getId(),
            amenity.getConsorcio().getId(),
            amenity.getNombre(),
            amenity.getDescripcion(),
            amenity.isActivo()
        );
    }
}