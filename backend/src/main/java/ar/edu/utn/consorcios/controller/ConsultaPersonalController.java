
package ar.edu.utn.consorcios.controller;

import ar.edu.utn.consorcios.dto.MiLiquidacionResponse;
import ar.edu.utn.consorcios.service.ConsultaPersonalService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mi-cuenta")
public class ConsultaPersonalController {

    private final ConsultaPersonalService service;

    public ConsultaPersonalController(
            ConsultaPersonalService service) {
        this.service = service;
    }

    @GetMapping("/liquidaciones")
    @PreAuthorize("hasAnyRole('PROPIETARIO','INQUILINO')")
    public List<MiLiquidacionResponse> misLiquidaciones(
            Authentication auth) {

        return service.misLiquidaciones(auth);
    }
}
