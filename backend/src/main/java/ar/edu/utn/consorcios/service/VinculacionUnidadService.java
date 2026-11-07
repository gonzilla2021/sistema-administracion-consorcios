
package ar.edu.utn.consorcios.service;

import ar.edu.utn.consorcios.dto.*;
import ar.edu.utn.consorcios.entity.*;
import ar.edu.utn.consorcios.repository.*;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class VinculacionUnidadService {

    private final VinculacionUnidadRepository vinculaciones;
    private final UnidadFuncionalRepository unidades;
    private final PersonaRepository personas;
    private final ConsorcioService consorcioService;

    public VinculacionUnidadService(
            VinculacionUnidadRepository vinculaciones,
            UnidadFuncionalRepository unidades,
            PersonaRepository personas,
            ConsorcioService consorcioService) {

        this.vinculaciones = vinculaciones;
        this.unidades = unidades;
        this.personas = personas;
        this.consorcioService = consorcioService;
    }

    // Comprueba acceso al consorcio y pertenencia de la unidad.
    private UnidadFuncional exigirUnidad(
            Authentication auth,
            UUID consorcioId,
            UUID unidadId) {

        consorcioService.exigirAcceso(auth, consorcioId);

        UnidadFuncional unidad = unidades.findById(unidadId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Unidad inexistente"
                ));

        if (!unidad.getConsorcio().getId().equals(consorcioId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "La unidad no pertenece al consorcio"
            );
        }

        return unidad;
    }

    // Crea una vinculación entre persona y unidad.
    @Transactional
    public VinculacionResponse crear(
            Authentication auth,
            UUID consorcioId,
            UUID unidadId,
            VinculacionRequest request) {

        UnidadFuncional unidad =
                exigirUnidad(auth, consorcioId, unidadId);

        validarFechas(request.fechaInicio(), request.fechaFin());

        Persona persona = personas.findById(request.personaId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Persona inexistente"
                ));

        if (!persona.getConsorcio().getId().equals(consorcioId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La persona no pertenece a este consorcio"
            );
        }

        VinculacionUnidad vinculacion = new VinculacionUnidad();

        vinculacion.setUnidadFuncional(unidad);
        vinculacion.setPersona(persona);
        vinculacion.setTipo(request.tipo());
        vinculacion.setFechaInicio(request.fechaInicio());
        vinculacion.setFechaFin(request.fechaFin());

        return convertir(vinculaciones.save(vinculacion));
    }

    // Lista las vinculaciones de una unidad.
    @Transactional(readOnly = true)
    public List<VinculacionResponse> listar(
            Authentication auth,
            UUID consorcioId,
            UUID unidadId) {

        exigirUnidad(auth, consorcioId, unidadId);

        return vinculaciones.findByUnidadFuncionalId(unidadId)
                .stream()
                .map(this::convertir)
                .toList();
    }

    // Finaliza una vinculación sin eliminarla.
    @Transactional
    public VinculacionResponse finalizar(
            Authentication auth,
            UUID consorcioId,
            UUID unidadId,
            UUID vinculacionId,
            LocalDate fechaFin) {

        exigirUnidad(auth, consorcioId, unidadId);

        VinculacionUnidad vinculacion =
                vinculaciones.findById(vinculacionId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Vinculación inexistente"
                        ));

        if (!vinculacion.getUnidadFuncional()
                .getId().equals(unidadId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "La vinculación no pertenece a la unidad"
            );
        }

        validarFechas(vinculacion.getFechaInicio(), fechaFin);

        vinculacion.setFechaFin(fechaFin);

        return convertir(vinculaciones.save(vinculacion));
    }

    // La fecha de fin no puede ser anterior al inicio.
    private void validarFechas(
            LocalDate inicio,
            LocalDate fin) {

        if (fin != null && fin.isBefore(inicio)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha de fin no puede ser anterior al inicio"
            );
        }
    }

    // Convierte la entidad en un DTO.
    private VinculacionResponse convertir(VinculacionUnidad v) {

        Persona p = v.getPersona();

        return new VinculacionResponse(
                v.getId(),
                v.getUnidadFuncional().getId(),
                p.getId(),
                p.getNombre(),
                p.getApellido(),
                v.getTipo(),
                v.getFechaInicio(),
                v.getFechaFin(),
                v.vigente()
        );
    }
}
