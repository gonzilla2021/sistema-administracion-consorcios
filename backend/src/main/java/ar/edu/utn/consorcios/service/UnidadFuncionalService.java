package ar.edu.utn.consorcios.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ar.edu.utn.consorcios.dto.UnidadRequest;
import ar.edu.utn.consorcios.dto.UnidadResponse;
import ar.edu.utn.consorcios.dto.UnidadesRequest;
import ar.edu.utn.consorcios.entity.Consorcio;
import ar.edu.utn.consorcios.entity.UnidadFuncional;
import ar.edu.utn.consorcios.repository.UnidadFuncionalRepository;

@Service
public class UnidadFuncionalService {

    private static final BigDecimal CIEN = new BigDecimal("100");
    private static final BigDecimal TOLERANCIA = new BigDecimal("0.0001");

    private final UnidadFuncionalRepository unidades;
    private final ConsorcioService consorcioService;

    public UnidadFuncionalService(UnidadFuncionalRepository unidades, ConsorcioService consorcioService) {
        this.unidades = unidades;
        this.consorcioService = consorcioService;
    }

    @Transactional(readOnly = true)
    public List<UnidadResponse> listar(Authentication auth, UUID consorcioId) {
        consorcioService.exigirAcceso(auth, consorcioId);
        return unidades.findByConsorcioIdOrderByIdentificador(consorcioId).stream().map(UnidadResponse::de).toList();
    }

    /**
     * Deja el consorcio con exactamente las unidades recibidas: actualiza las existentes por identificador,
     * crea las nuevas y borra las ausentes. La suma se valida antes de escribir porque la base solo la
     * controla al commit (trigger diferido) y fallaría con un error genérico.
     */
    @Transactional
    public List<UnidadResponse> reemplazar(Authentication auth, UUID consorcioId, UnidadesRequest request) {
        Consorcio consorcio = consorcioService.exigirAcceso(auth, consorcioId);
        List<UnidadRequest> nuevas = request.unidades();
        validar(nuevas);

        Map<String, UnidadFuncional> existentes = new HashMap<>();
        unidades.findByConsorcioIdOrderByIdentificador(consorcioId)
                .forEach(u -> existentes.put(u.getIdentificador(), u));

        Set<String> recibidos = new HashSet<>();
        for (UnidadRequest r : nuevas) {
            String identificador = r.identificador().trim();
            recibidos.add(identificador);
            UnidadFuncional u = existentes.get(identificador);
            if (u == null) {
                u = new UnidadFuncional();
                u.setConsorcio(consorcio);
                u.setIdentificador(identificador);
            }
            u.setCoeficientePropiedad(r.coeficientePropiedad().setScale(6, RoundingMode.HALF_UP));
            unidades.save(u);
        }
        List<UnidadFuncional> sobrantes = existentes.values().stream()
                .filter(u -> !recibidos.contains(u.getIdentificador())).toList();
        try {
            unidades.deleteAll(sobrantes);
            unidades.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se pueden quitar unidades con vinculaciones o liquidaciones asociadas");
        }
        return unidades.findByConsorcioIdOrderByIdentificador(consorcioId).stream().map(UnidadResponse::de).toList();
    }

    private void validar(List<UnidadRequest> nuevas) {
        Set<String> vistos = new HashSet<>();
        BigDecimal suma = BigDecimal.ZERO;
        for (UnidadRequest r : nuevas) {
            if (!vistos.add(r.identificador().trim())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Identificador repetido: " + r.identificador().trim());
            }
            suma = suma.add(r.coeficientePropiedad().setScale(6, RoundingMode.HALF_UP));
        }
        if (!nuevas.isEmpty() && suma.subtract(CIEN).abs().compareTo(TOLERANCIA) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La suma de coeficientes es " + suma.stripTrailingZeros().toPlainString()
                            + " y debe ser 100 (tolerancia 0.0001)");
        }
    }
}
