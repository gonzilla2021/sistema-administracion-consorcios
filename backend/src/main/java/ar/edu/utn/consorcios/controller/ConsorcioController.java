package ar.edu.utn.consorcios.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.utn.consorcios.dto.ConsorcioResponse;
import ar.edu.utn.consorcios.service.ConsorcioService;

@RestController
@RequestMapping("/api/consorcios")
public class ConsorcioController {

    private final ConsorcioService consorcioService;

    public ConsorcioController(ConsorcioService consorcioService) {
        this.consorcioService = consorcioService;
    }

    @GetMapping
    public List<ConsorcioResponse> listar(Authentication auth) {
        return consorcioService.listar(auth);
    }
}
