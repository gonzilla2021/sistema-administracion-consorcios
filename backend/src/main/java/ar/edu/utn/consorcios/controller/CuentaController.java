package ar.edu.utn.consorcios.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.utn.consorcios.dto.CambioPasswordRequest;
import ar.edu.utn.consorcios.service.CuentaService;
import jakarta.validation.Valid;

/** Operaciones del usuario autenticado sobre su propia cuenta (cualquier rol). */
@RestController
@RequestMapping("/api/cuenta")
public class CuentaController {

    private final CuentaService cuentaService;

    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarPassword(Authentication auth, @Valid @RequestBody CambioPasswordRequest request) {
        cuentaService.cambiarPassword(auth.getName(), request);
    }
}
