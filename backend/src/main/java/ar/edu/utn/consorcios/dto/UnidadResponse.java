package ar.edu.utn.consorcios.dto;

import java.math.BigDecimal;
import java.util.UUID;

import ar.edu.utn.consorcios.entity.UnidadFuncional;

public record UnidadResponse(UUID id, String identificador, BigDecimal coeficientePropiedad) {

    public static UnidadResponse de(UnidadFuncional u) {
        return new UnidadResponse(u.getId(), u.getIdentificador(), u.getCoeficientePropiedad());
    }
}
