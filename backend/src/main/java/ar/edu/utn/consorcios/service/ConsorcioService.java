package ar.edu.utn.consorcios.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ar.edu.utn.consorcios.dto.ConsorcioRequest;
import ar.edu.utn.consorcios.dto.ConsorcioResponse;
import ar.edu.utn.consorcios.entity.Consorcio;
import ar.edu.utn.consorcios.repository.ConsorcioRepository;
import ar.edu.utn.consorcios.repository.UnidadFuncionalRepository;

@Service
public class ConsorcioService {

    private final ConsorcioRepository consorcios;
    private final UnidadFuncionalRepository unidades;

    public ConsorcioService(ConsorcioRepository consorcios, UnidadFuncionalRepository unidades) {
        this.consorcios = consorcios;
        this.unidades = unidades;
    }

    /**
     * Superadministrador: todos. Administrador de consorcio: el suyo.
     * Propietarios e inquilinos: ninguno (su consulta por unidades es un módulo posterior).
     */
    @Transactional(readOnly = true)
    public List<ConsorcioResponse> listar(Authentication auth) {
        if (tieneRol(auth, "SUPERADMINISTRADOR")) {
            Map<UUID, Long> cantidades = unidades.contarPorConsorcio().stream()
                    .collect(Collectors.toMap(f -> (UUID) f[0], f -> (Long) f[1]));
            return consorcios.findAll().stream().map(c -> ConsorcioResponse.de(c, cantidades.getOrDefault(c.getId(), 0L))).toList();
        }
        if (tieneRol(auth, "ADMINISTRADOR_CONSORCIO")) {
            return consorcios.findAdministradoPor(auth.getName()).map(this::respuesta).stream().toList();
        }
        return List.of();
    }

    @Transactional(readOnly = true)
    public ConsorcioResponse obtener(Authentication auth, UUID id) {
        return respuesta(exigirAcceso(auth, id));
    }

    @Transactional
    public ConsorcioResponse crear(ConsorcioRequest request) {
        Consorcio c = new Consorcio();
        c.setNombre(request.nombre().trim());
        c.setDireccion(request.direccion().trim());
        return ConsorcioResponse.de(consorcios.save(c), 0);
    }

    @Transactional
    public ConsorcioResponse actualizar(Authentication auth, UUID id, ConsorcioRequest request) {
        Consorcio c = exigirAcceso(auth, id);
        c.setNombre(request.nombre().trim());
        c.setDireccion(request.direccion().trim());
        return respuesta(c);
    }

    /**
     * Devuelve el consorcio si existe y el usuario puede operar sobre él: el superadministrador sobre
     * cualquiera, el administrador de consorcio solo sobre el suyo. Para el resto responde 403.
     */
    @Transactional(readOnly = true)
    public Consorcio exigirAcceso(Authentication auth, UUID id) {
        Consorcio consorcio = consorcios.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Consorcio inexistente"));
        boolean permitido = tieneRol(auth, "SUPERADMINISTRADOR")
                || (tieneRol(auth, "ADMINISTRADOR_CONSORCIO")
                        && consorcios.findAdministradoPor(auth.getName()).map(c -> c.getId().equals(id)).orElse(false));
        if (!permitido) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin acceso a este consorcio");
        }
        return consorcio;
    }

    private ConsorcioResponse respuesta(Consorcio c) {
        return ConsorcioResponse.de(c, unidades.countByConsorcioId(c.getId()));
    }

    private boolean tieneRol(Authentication auth, String rol) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + rol));
    }
}
