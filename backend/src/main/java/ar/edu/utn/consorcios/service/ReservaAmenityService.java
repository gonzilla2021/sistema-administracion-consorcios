package ar.edu.utn.consorcios.service;

import ar.edu.utn.consorcios.dto.ReservaAmenityRequest;
import ar.edu.utn.consorcios.dto.ReservaAmenityResponse;
import ar.edu.utn.consorcios.entity.Amenity;
import ar.edu.utn.consorcios.entity.EstadoReservaAmenity;
import ar.edu.utn.consorcios.entity.Persona;
import ar.edu.utn.consorcios.entity.ReservaAmenity;
import ar.edu.utn.consorcios.entity.Rol;
import ar.edu.utn.consorcios.entity.TipoVinculo;
import ar.edu.utn.consorcios.entity.TurnoAmenity;
import ar.edu.utn.consorcios.entity.UnidadFuncional;
import ar.edu.utn.consorcios.entity.Usuario;
import ar.edu.utn.consorcios.entity.VinculacionUnidad;
import ar.edu.utn.consorcios.repository.BloqueoAmenityRepository;
import ar.edu.utn.consorcios.repository.ReservaAmenityRepository;
import ar.edu.utn.consorcios.repository.UsuarioRepository;
import ar.edu.utn.consorcios.repository.VinculacionUnidadRepository;
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
public class ReservaAmenityService {

    private final ReservaAmenityRepository reservaRepository;
    private final BloqueoAmenityRepository bloqueoRepository;
    private final UsuarioRepository usuarioRepository;
    private final VinculacionUnidadRepository vinculacionRepository;
    private final AmenityService amenityService;
    private final TurnoAmenityService turnoAmenityService;

    @Transactional
    public ReservaAmenityResponse crear(
        UUID amenityId,
        ReservaAmenityRequest request,
        Authentication authentication
    ) {
        Usuario usuario = exigirResidente(authentication);
        Persona persona = exigirPersona(usuario);

        Amenity amenity = exigirAmenityActivo(amenityId);

        TurnoAmenity turno = turnoAmenityService.exigirTurno(
            amenityId,
            request.turnoId()
        );

        exigirTurnoActivo(turno);
        validarFecha(request.fecha());

        UnidadFuncional unidad = buscarUnidadHabilitada(
            usuario,
            persona,
            amenity,
            request.fecha()
        );

        validarDisponibilidad(
            amenityId,
            turno.getId(),
            request.fecha()
        );

        ReservaAmenity reserva = new ReservaAmenity();
        reserva.setAmenity(amenity);
        reserva.setTurno(turno);
        reserva.setPersona(persona);
        reserva.setUnidadFuncional(unidad);
        reserva.setFecha(request.fecha());
        reserva.setEstado(EstadoReservaAmenity.ACTIVA);
        reserva.setFechaCreacion(LocalDateTime.now());
        reserva.setFechaCancelacion(null);

        try {
            return ReservaAmenityResponse.desde(
                reservaRepository.saveAndFlush(reserva)
            );
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "El turno acaba de ser reservado por otra persona"
            );
        }
    }

    @Transactional(readOnly = true)
    public List<ReservaAmenityResponse> listarPropias(
        Authentication authentication
    ) {
        Usuario usuario = exigirResidente(authentication);
        Persona persona = exigirPersona(usuario);

        return reservaRepository
            .findByPersonaIdOrderByFechaDesc(persona.getId())
            .stream()
            .map(ReservaAmenityResponse::desde)
            .toList();
    }

    @Transactional
    public ReservaAmenityResponse cancelarPropia(
        UUID reservaId,
        Authentication authentication
    ) {
        Usuario usuario = exigirResidente(authentication);
        Persona persona = exigirPersona(usuario);

        ReservaAmenity reserva = reservaRepository
            .findByIdAndPersonaId(
                reservaId,
                persona.getId()
            )
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Reserva no encontrada"
            ));

        if (reserva.getEstado() != EstadoReservaAmenity.ACTIVA) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "La reserva ya está cancelada"
            );
        }

        if (reserva.getFecha().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "No se puede cancelar una reserva de una fecha pasada"
            );
        }

        reserva.setEstado(
            EstadoReservaAmenity.CANCELADA_POR_USUARIO
        );
        reserva.setFechaCancelacion(LocalDateTime.now());

        return ReservaAmenityResponse.desde(
            reservaRepository.save(reserva)
        );
    }

    private Usuario exigirResidente(
        Authentication authentication
    ) {
        if (
            authentication == null
                || !authentication.isAuthenticated()
        ) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Usuario no autenticado"
            );
        }

        Usuario usuario = usuarioRepository
            .findByEmailIgnoreCase(authentication.getName())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Usuario no encontrado"
            ));

        if (!usuario.isActivo()) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Usuario inactivo"
            );
        }

        if (
            usuario.getRol() != Rol.PROPIETARIO
                && usuario.getRol() != Rol.INQUILINO
        ) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Solo propietarios e inquilinos pueden reservar"
            );
        }

        return usuario;
    }

    private Persona exigirPersona(Usuario usuario) {
        if (usuario.getPersona() == null) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "El usuario no tiene una persona asociada"
            );
        }

        return usuario.getPersona();
    }

    private Amenity exigirAmenityActivo(UUID amenityId) {
        Amenity amenity = amenityService.buscarPorId(amenityId);

        if (!amenity.isActivo()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "El amenity está inactivo"
            );
        }

        return amenity;
    }

    private void exigirTurnoActivo(TurnoAmenity turno) {
        if (!turno.isActivo()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "El turno está inactivo"
            );
        }
    }

    private void validarFecha(LocalDate fecha) {
        if (fecha.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La fecha no puede ser anterior a hoy"
            );
        }
    }

    private UnidadFuncional buscarUnidadHabilitada(
        Usuario usuario,
        Persona persona,
        Amenity amenity,
        LocalDate fechaReserva
    ) {
        return vinculacionRepository
            .findByPersonaId(persona.getId())
            .stream()
            .filter(vinculacion ->
                coincideRol(usuario, vinculacion)
            )
            .filter(vinculacion ->
                correspondeAlConsorcio(vinculacion, amenity)
            )
            .filter(vinculacion ->
                estaVigente(vinculacion, fechaReserva)
            )
            .map(VinculacionUnidad::getUnidadFuncional)
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "La persona no posee una vinculación vigente "
                    + "con una unidad del consorcio"
            ));
    }

    private boolean coincideRol(
        Usuario usuario,
        VinculacionUnidad vinculacion
    ) {
        if (usuario.getRol() == Rol.PROPIETARIO) {
            return vinculacion.getTipo()
                == TipoVinculo.PROPIETARIO;
        }

        if (usuario.getRol() == Rol.INQUILINO) {
            return vinculacion.getTipo()
                == TipoVinculo.INQUILINO;
        }

        return false;
    }

    private boolean correspondeAlConsorcio(
        VinculacionUnidad vinculacion,
        Amenity amenity
    ) {
        return vinculacion
            .getUnidadFuncional()
            .getConsorcio()
            .getId()
            .equals(amenity.getConsorcio().getId());
    }

    private boolean estaVigente(
        VinculacionUnidad vinculacion,
        LocalDate fecha
    ) {
        boolean yaComenzo =
            !vinculacion.getFechaInicio().isAfter(fecha);

        boolean noFinalizo =
            vinculacion.getFechaFin() == null
                || !vinculacion.getFechaFin().isBefore(fecha);

        return yaComenzo && noFinalizo;
    }

    private void validarDisponibilidad(
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
                "El turno ya está reservado"
            );
        }

        boolean bloqueado = bloqueoRepository
            .existsByAmenityIdAndTurnoIdAndFechaAndActivoTrue(
                amenityId,
                turnoId,
                fecha
            );

        if (bloqueado) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "El turno está bloqueado"
            );
        }
    }
}