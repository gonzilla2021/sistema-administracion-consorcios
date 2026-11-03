
package ar.edu.utn.consorcios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LiquidacionRequest(

        @NotBlank
        @Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])")
        String periodo

) {}
