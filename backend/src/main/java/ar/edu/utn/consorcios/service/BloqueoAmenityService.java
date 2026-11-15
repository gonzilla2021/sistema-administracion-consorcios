package ar.edu.utn.consorcios.service;

import ar.edu.utn.consorcios.dto.BloqueoAmenityRequest;
import ar.edu.utn.consorcios.dto.BloqueoAmenityResponse;
import ar.edu.utn.consorcios.entity.Amenity;
import ar.edu.utn.consorcios.entity.BloqueoAmenity;
import ar.edu.utn.consorcios.entity.EstadoReservaAmenity;
import ar.edu.utn.consorcios.entity.TurnoAmenity;
import ar.edu.utn.consorcios.repository.BloqueoAmenityRepository;
import ar.edu.utn.consorcios.repository.ReservaAmenityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BloqueoAmenityService {

    private final BloqueoAmenityRepository bloqueoRepository;
    private final ReservaAmenityRepository reservaRepository;
    private final ConsorcioService consorcioService;
    private final AmenityService amenityService;
    private final TurnoAmenityService turnoAmenityService;

    @Transactional(readOnly = true)
    public List<BloqueoAmenityResponse> listar(
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

        return bloqueoRepository
            .findByAmenityIdOrderByFechaDesc(amenityId)
            .stream()
            .map(BloqueoAmenityResponse::desde)
            .toList();
    }

    @Transactional
    public BloqueoAmenityResponse crear(
        UUID consorcioId,
        UUID amenityId,
        BloqueoAmenityRequest request,
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

        if (!amenity.isActivo()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "El amenity está inactivo"
            );
        }

        TurnoAmenity turno = turnoAmenityService.exigirTurno(
            amenityId,
            request.turnoId()
        );

        if (!turno.isActivo()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "El turno está inactivo"
            );
        }

        validarFecha(request.fecha());

        validarQueNoEsteReservado(
            amenityId,
            turno.getId(),
            request.fecha()
        );

        validarQueNoEsteBloqueado(
            amenityId,
            turno.getId(),
            request.fecha()
        );

        BloqueoAmenity bloqueo = new BloqueoAmenity();
        bloqueo.setAmenity(amenity);
        bloqueo.setTurno(turno);
        bloqueo.setFecha(request.fecha());
        bloqueo.setMotivo(request.motivo().trim());
        bloqueo.setFechaCreacion(LocalDateTime.now());
        bloqueo.setActivo(true);

        try {
            return BloqueoAmenityResponse.desde(
                bloqueoRepository.saveAndFlush(bloqueo)
            );
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "El turno ya está bloqueado"
            );
        }
    }

    @Transactional
    public BloqueoAmenityResponse desbloquear(
        UUID consorcioId,
        UUID amenityId,
        UUID bloqueoId,
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

        BloqueoAmenity bloqueo = bloqueoRepository
            .findByIdAndAmenityId(
                bloqueoId,
                amenityId
            )
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Bloqueo no encontrado"
            ));

        if (!bloqueo.isActivo()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "El turno ya está desbloqueado"
            );
        }

        bloqueo.setActivo(false);

        return BloqueoAmenityResponse.desde(
            bloqueoRepository.save(bloqueo)
        );
    }

    private void validarFecha(LocalDate fecha) {
        if (fecha.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "No se puede bloquear una fecha pasada"
            );
        }
    }

    private void validarQueNoEsteReservado(
        UUID amenityId,
        UUID turnoId,
        LocalDate fecha
    ) {
        boolean reservado = reservaRepository
            .existsByAmenityIdAndTurnoIdAndFechaAndEstado(
                amenityId,
                turnoId,
                fecha,
                EstadoReservaAmenity.ACTIVA
            );

        if (reservado) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "No se puede bloquear el turno porque "
                    + "ya tiene una reserva activa"
            );
        }
    }

    private void validarQueNoEsteBloqueado(
        UUID amenityId,
        UUID turnoId,
        LocalDate fecha
    ) {
        boolean bloqueado = bloqueoRepository
            .existsByAmenityIdAndTurnoIdAndFechaAndActivoTrue(
                amenityId,
                turnoId,
                fecha
            );

        if (bloqueado) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "El turno ya está bloqueado"
            );
        }
    }
}