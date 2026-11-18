package ar.edu.utn.consorcios.service;

import ar.edu.utn.consorcios.dto.AmenityRequest;
import ar.edu.utn.consorcios.dto.AmenityResponse;
import ar.edu.utn.consorcios.entity.Amenity;
import ar.edu.utn.consorcios.entity.Consorcio;
import ar.edu.utn.consorcios.repository.AmenityRepository;
import ar.edu.utn.consorcios.repository.ConsorcioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AmenityServiceTest {

    @Mock
    private AmenityRepository amenityRepository;

    @Mock
    private ConsorcioRepository consorcioRepository;

    @Mock
    private ConsorcioService consorcioService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AmenityService amenityService;

    private UUID consorcioId;
    private Consorcio consorcio;

    @BeforeEach
    void preparar() {
        consorcioId = UUID.randomUUID();

        consorcio = new Consorcio();
        consorcio.setId(consorcioId);
        consorcio.setNombre("Edificio Centro");
    }

    @Test
    void crearAmenityCorrectamente() {
        AmenityRequest request = new AmenityRequest(
            "SUM + Parrilla",
            "Salón de usos múltiples",
            true
        );

        when(
            consorcioService.exigirAcceso(
                authentication,
                consorcioId
            )
        ).thenReturn(consorcio);

        when(
            consorcioRepository.findById(consorcioId))
        .thenReturn(Optional.of(consorcio));

        when(
            amenityRepository
                .existsByConsorcioIdAndNombreIgnoreCase(
                    consorcioId,
                    "SUM + Parrilla"
                )
        ).thenReturn(false);

        when(amenityRepository.save(any(Amenity.class)))
            .thenAnswer(invocacion -> {
                Amenity amenity = invocacion.getArgument(0);
                amenity.setId(UUID.randomUUID());
                return amenity;
            });

        AmenityResponse respuesta = amenityService.crear(
            consorcioId,
            request,
            authentication
        );

        assertEquals("SUM + Parrilla", respuesta.nombre());
        assertEquals(consorcioId, respuesta.consorcioId());
        assertEquals(true, respuesta.activo());

        verify(amenityRepository).save(any(Amenity.class));
    }

    @Test
    void rechazarAmenityConNombreDuplicado() {
        AmenityRequest request = new AmenityRequest(
            "SUM + Parrilla",
            null,
            true
        );

        when(
            consorcioService.exigirAcceso(
                authentication,
                consorcioId
            )
        ).thenReturn(consorcio);

        when(consorcioRepository.findById(consorcioId))
        .thenReturn(Optional.of(consorcio));

        when(
            amenityRepository
                .existsByConsorcioIdAndNombreIgnoreCase(
                    consorcioId,
                    "SUM + Parrilla"
                )
        ).thenReturn(true);

        ResponseStatusException error = assertThrows(
            ResponseStatusException.class,
            () -> amenityService.crear(
                consorcioId,
                request,
                authentication
            )
        );

        assertEquals(409, error.getStatusCode().value());
    }
}