package ar.edu.utn.consorcios.service;

import ar.edu.utn.consorcios.dto.DisponibilidadAmenityResponse;
import ar.edu.utn.consorcios.dto.ReservaAmenityRequest;
import ar.edu.utn.consorcios.dto.ReservaAmenityResponse;
import ar.edu.utn.consorcios.entity.Amenity;
import ar.edu.utn.consorcios.dto.AmenityResponse;
import ar.edu.utn.consorcios.repository.AmenityRepository;
import ar.edu.utn.consorcios.entity.BloqueoAmenity;
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
import ar.edu.utn.consorcios.repository.TurnoAmenityRepository;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservaAmenityService {

    private final ReservaAmenityRepository reservaRepository;
    private final BloqueoAmenityRepository bloqueoRepository;
    private final TurnoAmenityRepository turnoRepository;
    private final AmenityRepository amenityRepository;
    private final UsuarioRepository usuarioRepository;
    private final VinculacionUnidadRepository vinculacionRepository;
    private final AmenityService amenityService;
    private final TurnoAmenityService turnoAmenityService;
    private final ConsorcioService consorcioService;

    /**
     * Crea una reserva para la persona asociada al usuario autenticado.
     */
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
        validarFechaReserva(request.fecha());

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
            ReservaAmenity guardada =
                reservaRepository.saveAndFlush(reserva);

            return ReservaAmenityResponse.desde(guardada);
        } catch (DataIntegrityViolationException ex) {
            /*
             * El índice único parcial de PostgreSQL impide que dos
             * solicitudes simultáneas creen reservas activas para
             * el mismo amenity, turno y fecha.
             */
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "El turno acaba de ser reservado por otra persona"
            );
        }
    }

    /**
     * Devuelve todas las reservas de la persona autenticada.
     */
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

    @Transactional(readOnly = true)
    public List<ReservaAmenityResponse> listarAdministrativas(
        UUID consorcioId,
        UUID amenityId,
        LocalDate desde,
        LocalDate hasta,
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

        validarRangoAdministrativo(desde, hasta);

        return reservaRepository
            .findByAmenityIdAndFechaBetweenOrderByFechaAsc(
                amenityId,
                desde,
                hasta
            )
            .stream()
            .map(ReservaAmenityResponse::desde)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<AmenityResponse> listarAmenitiesAccesibles(
        Authentication authentication
    ) {
        Usuario usuario = exigirResidente(authentication);
        Persona persona = exigirPersona(usuario);
        LocalDate hoy = LocalDate.now();

        List<UUID> consorcioIds = vinculacionRepository
            .findByPersonaId(persona.getId())
            .stream()
            .filter(vinculacion ->
                coincideRol(usuario, vinculacion)
            )
            .filter(vinculacion ->
                estaVigente(vinculacion, hoy)
            )
            .map(VinculacionUnidad::getUnidadFuncional)
            .filter(Objects::nonNull)
            .map(UnidadFuncional::getConsorcio)
            .filter(Objects::nonNull)
            .map(consorcio -> consorcio.getId())
            .distinct()
            .toList();

        if (consorcioIds.isEmpty()) {
            return List.of();
        }

        return amenityRepository
            .findByConsorcioIdInAndActivoTrueOrderByNombre(
                consorcioIds
            )
            .stream()
            .map(AmenityResponse::desde)
            .toList();
    }

    /**
     * Cancela una reserva activa perteneciente a la persona autenticada.
     */
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

    /**
     * Calcula la disponibilidad de todos los turnos activos
     * del amenity para un rango máximo de 31 días.
     */
    @Transactional(readOnly = true)
    public List<DisponibilidadAmenityResponse> consultarDisponibilidad(
        UUID amenityId,
        LocalDate desde,
        LocalDate hasta,
        Authentication authentication
    ) {
        Usuario usuario = exigirResidente(authentication);
        Persona persona = exigirPersona(usuario);

        Amenity amenity = exigirAmenityActivo(amenityId);

        validarRangoDisponibilidad(desde, hasta);

        /*
         * Primero comprobamos que la persona tenga acceso al
         * consorcio del amenity en el comienzo del rango.
         */
        buscarUnidadHabilitada(
            usuario,
            persona,
            amenity,
            desde
        );

        List<TurnoAmenity> turnos = turnoRepository
            .findByAmenityIdAndActivoTrueOrderByHoraInicio(
                amenityId
            );

        if (turnos.isEmpty()) {
            return List.of();
        }

        List<ReservaAmenity> reservasActivas = reservaRepository
            .findByAmenityIdAndFechaBetweenAndEstadoOrderByFechaAsc(
                amenityId,
                desde,
                hasta,
                EstadoReservaAmenity.ACTIVA
            );

        List<BloqueoAmenity> bloqueosActivos = bloqueoRepository
            .findByAmenityIdAndFechaBetweenAndActivoTrueOrderByFechaAsc(
                amenityId,
                desde,
                hasta
            );

        /*
         * Se indexan las reservas por turno y fecha.
         * Esto evita consultar la base dentro de cada iteración.
         */
        Map<String, ReservaAmenity> reservasPorTurnoYFecha =
            new HashMap<>();

        for (ReservaAmenity reserva : reservasActivas) {
            String clave = generarClave(
                reserva.getTurno().getId(),
                reserva.getFecha()
            );

            reservasPorTurnoYFecha.put(clave, reserva);
        }

        /*
         * Para los bloqueos solo interesa saber si existe uno
         * activo para el turno y la fecha.
         */
        Set<String> bloqueosPorTurnoYFecha = new HashSet<>();

        for (BloqueoAmenity bloqueo : bloqueosActivos) {
            String clave = generarClave(
                bloqueo.getTurno().getId(),
                bloqueo.getFecha()
            );

            bloqueosPorTurnoYFecha.add(clave);
        }

        List<DisponibilidadAmenityResponse> resultado =
            new ArrayList<>();

        LocalDate fechaActual = desde;

        while (!fechaActual.isAfter(hasta)) {
            for (TurnoAmenity turno : turnos) {
                String clave = generarClave(
                    turno.getId(),
                    fechaActual
                );

                ReservaAmenity reserva =
                    reservasPorTurnoYFecha.get(clave);

                boolean bloqueado =
                    bloqueosPorTurnoYFecha.contains(clave);

                boolean disponible;
                String estado;

                /*
                 * No debería existir simultáneamente una reserva
                 * activa y un bloqueo activo. Si por datos antiguos
                 * ocurriera, la reserva tiene prioridad visual.
                 */
                if (reserva != null) {
                    boolean reservaPropia = reserva
                        .getPersona()
                        .getId()
                        .equals(persona.getId());

                    disponible = false;
                    estado = reservaPropia
                        ? "RESERVA_PROPIA"
                        : "RESERVADO";
                } else if (bloqueado) {
                    disponible = false;
                    estado = "BLOQUEADO";
                } else {
                    disponible = true;
                    estado = "DISPONIBLE";
                }

                resultado.add(
                    new DisponibilidadAmenityResponse(
                        turno.getId(),
                        turno.getNombre(),
                        turno.getHoraInicio(),
                        turno.getHoraFin(),
                        fechaActual,
                        disponible,
                        estado
                    )
                );
            }

            fechaActual = fechaActual.plusDays(1);
        }

        return resultado;
    }

    /**
     * Obtiene y valida al usuario autenticado.
     */
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
                "Solo propietarios e inquilinos pueden "
                    + "utilizar esta operación"
            );
        }

        return usuario;
    }

    /**
     * Exige que la cuenta esté asociada a una persona.
     */
    private Persona exigirPersona(Usuario usuario) {
        if (usuario.getPersona() == null) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "El usuario no tiene una persona asociada"
            );
        }

        return usuario.getPersona();
    }

    /**
     * Obtiene el amenity y valida que esté activo.
     */
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

    /**
     * Valida que el turno pueda recibir reservas.
     */
    private void exigirTurnoActivo(TurnoAmenity turno) {
        if (!turno.isActivo()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "El turno está inactivo"
            );
        }
    }

    /**
     * Validación de la fecha concreta de una reserva.
     */
    private void validarFechaReserva(LocalDate fecha) {
        if (fecha.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La fecha no puede ser anterior a hoy"
            );
        }
    }

    /**
     * Valida el rango solicitado para consultar disponibilidad.
     */
    private void validarRangoDisponibilidad(
        LocalDate desde,
        LocalDate hasta
    ) {
        if (desde == null || hasta == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Las fechas desde y hasta son obligatorias"
            );
        }

        if (desde.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La fecha desde no puede ser anterior a hoy"
            );
        }

        if (hasta.isBefore(desde)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La fecha hasta no puede ser anterior "
                    + "a la fecha desde"
            );
        }

        /*
         * Desde más 30 días produce un rango de 31 fechas
         * contando ambos extremos.
         */
        if (hasta.isAfter(desde.plusDays(30))) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El rango de disponibilidad no puede "
                    + "superar los 31 días"
            );
        }
    }

    /**
     * Busca una unidad vinculada a la persona que:
     *
     * 1. Tenga el mismo tipo que el rol de la cuenta.
     * 2. Pertenezca al consorcio del amenity.
     * 3. Tenga una vinculación vigente en la fecha consultada.
     */
    private UnidadFuncional buscarUnidadHabilitada(
        Usuario usuario,
        Persona persona,
        Amenity amenity,
        LocalDate fecha
    ) {
        return vinculacionRepository
            .findByPersonaId(persona.getId())
            .stream()
            .filter(vinculacion ->
                coincideRol(usuario, vinculacion)
            )
            .filter(vinculacion ->
                correspondeAlConsorcio(
                    vinculacion,
                    amenity
                )
            )
            .filter(vinculacion ->
                estaVigente(vinculacion, fecha)
            )
            .map(VinculacionUnidad::getUnidadFuncional)
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "La persona no posee una vinculación vigente "
                    + "con una unidad del consorcio"
            ));
    }

    /**
     * Una cuenta propietaria solo puede utilizar vínculos de
     * tipo PROPIETARIO y una cuenta inquilina, vínculos INQUILINO.
     */
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

    /**
     * Comprueba que la unidad vinculada pertenezca al mismo
     * consorcio que el amenity.
     */
    private boolean correspondeAlConsorcio(
        VinculacionUnidad vinculacion,
        Amenity amenity
    ) {
        return vinculacion
            .getUnidadFuncional()
            .getConsorcio()
            .getId()
            .equals(
                amenity.getConsorcio().getId()
            );
    }

    /**
     * La vinculación es válida si comenzó antes o en la fecha
     * consultada y no finalizó antes de esa fecha.
     */
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

    /**
     * Verifica disponibilidad al momento de crear una reserva.
     */
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

    /**
     * Genera una clave única en memoria para una combinación
     * de turno y fecha.
     */
    private String generarClave(
        UUID turnoId,
        LocalDate fecha
    ) {
        return turnoId + "|" + fecha;
    }

    private void validarRangoAdministrativo(
        LocalDate desde,
        LocalDate hasta
    ) {
        if (desde == null || hasta == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Las fechas desde y hasta son obligatorias"
            );
        }

        if (hasta.isBefore(desde)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La fecha hasta no puede ser anterior "
                    + "a la fecha desde"
            );
        }

        if (hasta.isAfter(desde.plusDays(30))) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El rango de consulta no puede superar los 31 días"
            );
        }
    }
}