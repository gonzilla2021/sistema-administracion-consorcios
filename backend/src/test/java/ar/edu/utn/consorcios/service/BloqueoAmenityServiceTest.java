package ar.edu.utn.consorcios.service;

import ar.edu.utn.consorcios.dto.BloqueoAmenityRequest;
import ar.edu.utn.consorcios.dto.BloqueoAmenityResponse;
import ar.edu.utn.consorcios.entity.Amenity;
import ar.edu.utn.consorcios.entity.BloqueoAmenity;
import ar.edu.utn.consorcios.entity.Consorcio;
import ar.edu.utn.consorcios.entity.EstadoReservaAmenity;
import ar.edu.utn.consorcios.entity.TurnoAmenity;
import ar.edu.utn.consorcios.repository.BloqueoAmenityRepository;
import ar.edu.utn.consorcios.repository.ReservaAmenityRepository;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BloqueoAmenityServiceTest {

    @Mock
    private BloqueoAmenityRepository bloqueoRepository;

    @Mock
    private ReservaAmenityRepository reservaRepository;

    @Mock
    private ConsorcioService consorcioService;

    @Mock
    private AmenityService amenityService;

    @Mock
    private TurnoAmenityService turnoAmenityService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private BloqueoAmenityService bloqueoService;

    private UUID consorcioId;
    private UUID amenityId;
    private UUID turnoId;
    private LocalDate fecha;

    private Consorcio consorcio;
    private Amenity amenity;
    private TurnoAmenity turno;

    @BeforeEach
    void preparar() {
        consorcioId = UUID.randomUUID();
        amenityId = UUID.randomUUID();
        turnoId = UUID.randomUUID();
        fecha = LocalDate.now().plusDays(7);

        consorcio = new Consorcio();
        consorcio.setId(consorcioId);

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
    }

    @Test
    void bloquearTurnoLibreCorrectamente() {
        BloqueoAmenityRequest request =
            new BloqueoAmenityRequest(
                turnoId,
                fecha,
                "Mantenimiento de la parrilla"
            );

        when(
            consorcioService.exigirAcceso(
                authentication,
                consorcioId
            )
        ).thenReturn(consorcio);

        when(
            amenityService.exigirAmenity(
                consorcioId,
                amenityId
            )
        ).thenReturn(amenity);

        when(
            turnoAmenityService.exigirTurno(
                amenityId,
                turnoId
            )
        ).thenReturn(turno);

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

        when(bloqueoRepository.saveAndFlush(any()))
            .thenAnswer(invocacion -> {
                BloqueoAmenity bloqueo =
                    invocacion.getArgument(0);

                bloqueo.setId(UUID.randomUUID());
                return bloqueo;
            });

        BloqueoAmenityResponse respuesta =
            bloqueoService.crear(
                consorcioId,
                amenityId,
                request,
                authentication
            );

        assertEquals("SUM + Parrilla", respuesta.amenity());
        assertEquals("Noche", respuesta.turno());
        assertEquals(true, respuesta.activo());

        verify(bloqueoRepository).saveAndFlush(
            any(BloqueoAmenity.class)
        );
    }

    @Test
    void rechazarBloqueoDeTurnoReservado() {
        BloqueoAmenityRequest request =
            new BloqueoAmenityRequest(
                turnoId,
                fecha,
                "Mantenimiento"
            );

        when(
            consorcioService.exigirAcceso(
                authentication,
                consorcioId
            )
        ).thenReturn(consorcio);

        when(
            amenityService.exigirAmenity(
                consorcioId,
                amenityId
            )
        ).thenReturn(amenity);

        when(
            turnoAmenityService.exigirTurno(
                amenityId,
                turnoId
            )
        ).thenReturn(turno);

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
            () -> bloqueoService.crear(
                consorcioId,
                amenityId,
                request,
                authentication
            )
        );

        assertEquals(409, error.getStatusCode().value());

        verify(bloqueoRepository, never())
            .saveAndFlush(any());
    }
}