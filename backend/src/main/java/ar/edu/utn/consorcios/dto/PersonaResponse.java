
package ar.edu.utn.consorcios.dto;

import java.util.UUID;

// DTO que devuelve los datos de una persona al frontend.
public record PersonaResponse(

        UUID id,
        String nombre,
        String apellido,
        String dni,
        String email,
        String telefono

) {}
