package ar.edu.utn.consorcios.dto;

import ar.edu.utn.consorcios.entity.Rol;

public record LoginResponse(String token, String email, Rol rol) {
}
