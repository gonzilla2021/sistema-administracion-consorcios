package ar.edu.utn.consorcios.service;

import ar.edu.utn.consorcios.dto.TurnoAmenityRequest;
import ar.edu.utn.consorcios.dto.TurnoAmenityResponse;
import ar.edu.utn.consorcios.entity.Amenity;
import ar.edu.utn.consorcios.entity.TurnoAmenity;
import ar.edu.utn.consorcios.repository.TurnoAmenityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TurnoAmenityService {

    private final TurnoAmenityRepository turnoRepository;
    private final AmenityService amenityService;
    private final ConsorcioService consorcioService;

    @Transactional(readOnly = true)
    public List<TurnoAmenityResponse> listar(
        UUID consorcioId,
        UUID amenityId,
        Authentication authentication
    ) {
        consorcioService.exigirAcceso(
            authentication,
            consorcioId
        );

        amenityService.exigirAmenity(
            consorcioId,
            amenityId
        );

        return turnoRepository
            .findByAmenityIdOrderByHoraInicio(amenityId)
            .stream()
            .map(TurnoAmenityResponse::desde)
            .toList();
    }

    @Transactional(readOnly = true)
    public TurnoAmenityResponse obtener(
        UUID consorcioId,
        UUID amenityId,
        UUID turnoId,
        Authentication authentication
    ) {
        consorcioService.exigirAcceso(
            authentication,
            consorcioId
        );

        amenityService.exigirAmenity(
            consorcioId,
            amenityId
        );

        return TurnoAmenityResponse.desde(
            exigirTurno(amenityId, turnoId)
        );
    }

    @Transactional
    public TurnoAmenityResponse crear(
        UUID consorcioId,
        UUID amenityId,
        TurnoAmenityRequest request,
        Authentication authentication
    ) {
        consorcioService.exigirAcceso(
            authentication,
            consorcioId
        );

        Amenity amenity = amenityService.exigirAmenity(
            consorcioId,
            amenityId
        );

        validarAmenityActivo(amenity);
        validarHorario(
            request.horaInicio(),
            request.horaFin()
        );

        String nombre = normalizarNombre(request.nombre());

        boolean nombreExistente = turnoRepository
            .existsByAmenityIdAndNombreIgnoreCase(
                amenityId,
                nombre
            );

        if (nombreExistente) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Ya existe un turno con ese nombre"
            );
        }

        validarSuperposicion(
            amenityId,
            null,
            request.horaInicio(),
            request.horaFin()
        );

        TurnoAmenity turno = new TurnoAmenity();
        turno.setAmenity(amenity);
        turno.setNombre(nombre);
        turno.setHoraInicio(request.horaInicio());
        turno.setHoraFin(request.horaFin());
        turno.setActivo(request.activo());

        return TurnoAmenityResponse.desde(
            turnoRepository.save(turno)
        );
    }

    @Transactional
    public TurnoAmenityResponse actualizar(
        UUID consorcioId,
        UUID amenityId,
        UUID turnoId,
        TurnoAmenityRequest request,
        Authentication authentication
    ) {
        consorcioService.exigirAcceso(
            authentication,
            consorcioId
        );

        Amenity amenity = amenityService.exigirAmenity(
            consorcioId,
            amenityId
        );

        validarAmenityActivo(amenity);

        TurnoAmenity turno = exigirTurno(
            amenityId,
            turnoId
        );

        validarHorario(
            request.horaInicio(),
            request.horaFin()
        );

        String nombre = normalizarNombre(request.nombre());

        boolean nombreOcupado = turnoRepository
            .existsByAmenityIdAndNombreIgnoreCaseAndIdNot(
                amenityId,
                nombre,
                turnoId
            );

        if (nombreOcupado) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Ya existe otro turno con ese nombre"
            );
        }

        validarSuperposicion(
            amenityId,
            turnoId,
            request.horaInicio(),
            request.horaFin()
        );

        turno.setNombre(nombre);
        turno.setHoraInicio(request.horaInicio());
        turno.setHoraFin(request.horaFin());
        turno.setActivo(request.activo());

        return TurnoAmenityResponse.desde(
            turnoRepository.save(turno)
        );
    }

    @Transactional
    public TurnoAmenityResponse cambiarEstado(
        UUID consorcioId,
        UUID amenityId,
        UUID turnoId,
        boolean activo,
        Authentication authentication
    ) {
        consorcioService.exigirAcceso(
            authentication,
            consorcioId
        );

        Amenity amenity = amenityService.exigirAmenity(
            consorcioId,
            amenityId
        );

        if (activo) {
            validarAmenityActivo(amenity);
        }

        TurnoAmenity turno = exigirTurno(
            amenityId,
            turnoId
        );

        if (activo) {
            validarSuperposicion(
                amenityId,
                turnoId,
                turno.getHoraInicio(),
                turno.getHoraFin()
            );
        }

        turno.setActivo(activo);

        return TurnoAmenityResponse.desde(
            turnoRepository.save(turno)
        );
    }

    @Transactional(readOnly = true)
    public TurnoAmenity exigirTurno(
        UUID amenityId,
        UUID turnoId
    ) {
        return turnoRepository
            .findByIdAndAmenityId(turnoId, amenityId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Turno no encontrado"
            ));
    }

    private void validarAmenityActivo(Amenity amenity) {
        if (!amenity.isActivo()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "No se pueden administrar turnos "
                    + "de un amenity inactivo"
            );
        }
    }

    private void validarHorario(
        LocalTime horaInicio,
        LocalTime horaFin
    ) {
        if (!horaFin.isAfter(horaInicio)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La hora de finalización debe ser posterior "
                    + "a la hora de inicio"
            );
        }
    }

    private void validarSuperposicion(
        UUID amenityId,
        UUID turnoIdExcluido,
        LocalTime horaInicio,
        LocalTime horaFin
    ) {
        boolean superpuesto = turnoRepository
            .findByAmenityIdOrderByHoraInicio(amenityId)
            .stream()
            .filter(TurnoAmenity::isActivo)
            .filter(turno ->
                turnoIdExcluido == null
                    || !turno.getId().equals(turnoIdExcluido)
            )
            .anyMatch(turno ->
                horaInicio.isBefore(turno.getHoraFin())
                    && horaFin.isAfter(turno.getHoraInicio())
            );

        if (superpuesto) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "El horario se superpone con otro turno activo"
            );
        }
    }

    private String normalizarNombre(String nombre) {
        return nombre.trim();
    }
}