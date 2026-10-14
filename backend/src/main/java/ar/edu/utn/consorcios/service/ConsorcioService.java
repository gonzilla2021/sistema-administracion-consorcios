package ar.edu.utn.consorcios.service;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.utn.consorcios.dto.ConsorcioResponse;
import ar.edu.utn.consorcios.repository.ConsorcioRepository;

@Service
public class ConsorcioService {

    private final ConsorcioRepository consorcios;

    public ConsorcioService(ConsorcioRepository consorcios) {
        this.consorcios = consorcios;
    }

    /**
     * Superadministrador: todos. Administrador de consorcio: el suyo.
     * Propietarios e inquilinos: ninguno (su consulta por unidades es un módulo posterior).
     */
    @Transactional(readOnly = true)
    public List<ConsorcioResponse> listar(Authentication auth) {
        if (tieneRol(auth, "SUPERADMINISTRADOR")) {
            return consorcios.findAll().stream().map(ConsorcioResponse::de).toList();
        }
        if (tieneRol(auth, "ADMINISTRADOR_CONSORCIO")) {
            return consorcios.findAdministradoPor(auth.getName()).map(ConsorcioResponse::de).stream().toList();
        }
        return List.of();
    }

    private boolean tieneRol(Authentication auth, String rol) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + rol));
    }
}
