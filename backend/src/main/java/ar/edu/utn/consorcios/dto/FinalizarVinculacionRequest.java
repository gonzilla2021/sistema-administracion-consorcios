
package ar.edu.utn.consorcios.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

// Fecha en la que termina una vinculación.
public record FinalizarVinculacionRequest(

        @NotNull
        LocalDate fechaFin

) {}
