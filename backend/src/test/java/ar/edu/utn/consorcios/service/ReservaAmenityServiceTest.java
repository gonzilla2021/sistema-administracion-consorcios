package ar.edu.utn.consorcios.service;

import ar.edu.utn.consorcios.dto.ReservaAmenityRequest;
import ar.edu.utn.consorcios.dto.ReservaAmenityResponse;
import ar.edu.utn.consorcios.entity.Amenity;
import ar.edu.utn.consorcios.entity.Consorcio;
import ar.edu.utn.consorcios.entity.EstadoReservaAmenity;
import ar.edu.utn.consorcios.entity.Persona;
import ar.edu.utn.consorcios.entity.ReservaAmenity;
import ar.edu.utn.consorcios.entity.Rol;
import ar.edu.utn.consorcios.entity.TipoVinculo;
import ar.edu.utn.consorcios.entity.TurnoAmenity;
import ar.edu.utn.consorcios.entity.UnidadFuncional;
import ar.edu.utn.consorcios.entity.Usuario;
import ar.edu.utn.consorcios.entity.VinculacionUnidad;
import ar.edu.utn.consorcios.repository.AmenityRepository;
import ar.edu.utn.consorcios.repository.BloqueoAmenityRepository;
import ar.edu.utn.consorcios.repository.ReservaAmenityRepository;
import ar.edu.utn.consorcios.repository.TurnoAmenityRepository;
import ar.edu.utn.consorcios.repository.UsuarioRepository;
import ar.edu.utn.consorcios.repository.VinculacionUnidadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaAmenityServiceTest {

    @Mock
    private ReservaAmenityRepository reservaRepository;

    @Mock
    private BloqueoAmenityRepository bloqueoRepository;

    @Mock
    private TurnoAmenityRepository turnoRepository;

    @Mock
    private AmenityRepository amenityRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private VinculacionUnidadRepository vinculacionRepository;

    @Mock
    private AmenityService amenityService;

    @Mock
    private TurnoAmenityService turnoAmenityService;

    @Mock
    private ConsorcioService consorcioService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ReservaAmenityService reservaService;

    private UUID amenityId;
    private UUID turnoId;
    private UUID personaId;
    private UUID unidadId;

    private Usuario usuario;
    private Persona persona;
    private Consorcio consorcio;
    private UnidadFuncional unidad;
    private Amenity amenity;
    private TurnoAmenity turno;
    private VinculacionUnidad vinculacion;
    private LocalDate fecha;

    @BeforeEach
    void preparar() {
        amenityId = UUID.randomUUID();
        turnoId = UUID.randomUUID();
        personaId = UUID.randomUUID();
        unidadId = UUID.randomUUID();
        fecha = LocalDate.now().plusDays(5);

        persona = new Persona();
        persona.setId(personaId);
        persona.setNombre("Ana");
        persona.setApellido("Pérez");

        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setEmail("propietario@test.com");
        usuario.setActivo(true);
        usuario.setRol(Rol.PROPIETARIO);
        usuario.setPersona(persona);

        consorcio = new Consorcio();
        consorcio.setId(UUID.randomUUID());
        consorcio.setNombre("Edificio Centro");

        unidad = new UnidadFuncional();
        unidad.setId(unidadId);
        unidad.setIdentificador("1A");
        unidad.setConsorcio(consorcio);

        amenity = new Amenity();
        amenity.setId(amenityId);
        amenity.setNombre("SUM + Parrilla");
        amenity.setActivo(true);
        amenity.setConsorcio(consorcio);

        turno = new TurnoAmenity();
        turno.setId(turnoId);
        turno.setAmenity(amenity);
        turno.setNombre("Noche");
        turno.setHoraInicio(LocalTime.of(18, 0));
        turno.setHoraFin(LocalTime.of(23, 59));
        turno.setActivo(true);

        vinculacion = new VinculacionUnidad();
        vinculacion.setId(UUID.randomUUID());
        vinculacion.setPersona(persona);
        vinculacion.setUnidadFuncional(unidad);
        vinculacion.setTipo(TipoVinculo.PROPIETARIO);
        vinculacion.setFechaInicio(LocalDate.now().minusYears(1));

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName())
            .thenReturn("propietario@test.com");

        when(
            usuarioRepository.findByEmailIgnoreCase(
                "propietario@test.com"
            )
        ).thenReturn(Optional.of(usuario));

        when(amenityService.buscarPorId(amenityId))
            .thenReturn(amenity);

        when(
            turnoAmenityService.exigirTurno(
                amenityId,
                turnoId
            )
        ).thenReturn(turno);

        when(
            vinculacionRepository.findByPersonaId(personaId)
        ).thenReturn(List.of(vinculacion));
    }

    @Test
    void crearReservaCorrectamente() {
        ReservaAmenityRequest request =
            new ReservaAmenityRequest(turnoId, fecha);

        when(
            reservaRepository
                .existsByAmenityIdAndTurnoIdAndFechaAndEstado(
                    amenityId,
                    turnoId,
                    fecha,
                    EstadoReservaAmenity.ACTIVA
                )
        ).thenReturn(false);

        when(
            bloqueoRepository
                .existsByAmenityIdAndTurnoIdAndFechaAndActivoTrue(
                    amenityId,
                    turnoId,
                    fecha
                )
        ).thenReturn(false);

        when(reservaRepository.saveAndFlush(any()))
            .thenAnswer(invocacion -> {
                ReservaAmenity reserva =
                    invocacion.getArgument(0);

                reserva.setId(UUID.randomUUID());
                return reserva;
            });

        ReservaAmenityResponse respuesta =
            reservaService.crear(
                amenityId,
                request,
                authentication
            );

        assertEquals("SUM + Parrilla", respuesta.amenity());
        assertEquals("Noche", respuesta.turno());
        assertEquals("1A", respuesta.unidad());
        assertEquals(EstadoReservaAmenity.ACTIVA, respuesta.estado());

        verify(reservaRepository).saveAndFlush(
            any(ReservaAmenity.class)
        );
    }

    @Test
    void rechazarTurnoYaReservado() {
        ReservaAmenityRequest request =
            new ReservaAmenityRequest(turnoId, fecha);

        when(
            reservaRepository
                .existsByAmenityIdAndTurnoIdAndFechaAndEstado(
                    amenityId,
                    turnoId,
                    fecha,
                    EstadoReservaAmenity.ACTIVA
                )
        ).thenReturn(true);

        ResponseStatusException error = assertThrows(
            ResponseStatusException.class,
            () -> reservaService.crear(
                amenityId,
                request,
                authentication
            )
        );

        assertEquals(409, error.getStatusCode().value());

        verify(reservaRepository, never())
            .saveAndFlush(any());
    }
}