
package ar.edu.utn.consorcios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// DTO para recibir los datos de una persona
public record PersonaRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100)
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 100)
        String apellido,

        @NotBlank(message = "El DNI es obligatorio")
        @Size(max = 10)
        String dni,

        @Email(message = "El email no es válido")
        @Size(max = 254)
        String email,

        @Size(max = 30)
        String telefono

) {}
