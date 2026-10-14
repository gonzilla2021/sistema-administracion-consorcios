package ar.edu.utn.consorcios.dto;

import java.time.LocalDate;
import java.util.UUID;

import ar.edu.utn.consorcios.entity.Consorcio;

public record ConsorcioResponse(UUID id, String nombre, String direccion, LocalDate fechaAlta) {

    public static ConsorcioResponse de(Consorcio c) {
        return new ConsorcioResponse(c.getId(), c.getNombre(), c.getDireccion(), c.getFechaAlta());
    }
}
