
package ar.edu.utn.consorcios.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record PagoRequest(

        @NotNull
        @PastOrPresent
        LocalDate fechaPago

) {}

