package ar.edu.utn.consorcios.service;

import ar.edu.utn.consorcios.dto.AmenityRequest;
import ar.edu.utn.consorcios.dto.AmenityResponse;
import ar.edu.utn.consorcios.entity.Amenity;
import ar.edu.utn.consorcios.entity.Consorcio;
import ar.edu.utn.consorcios.repository.AmenityRepository;
import ar.edu.utn.consorcios.repository.ConsorcioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AmenityService {

    private final AmenityRepository amenityRepository;
    private final ConsorcioRepository consorcioRepository;
    private final ConsorcioService consorcioService;

    @Transactional(readOnly = true)
    public List<AmenityResponse> listar(
        UUID consorcioId,
        Authentication authentication
    ) {
        consorcioService.exigirAcceso(
            authentication,
            consorcioId
        );

        return amenityRepository
            .findByConsorcioIdOrderByNombre(consorcioId)
            .stream()
            .map(AmenityResponse::desde)
            .toList();
    }

    @Transactional(readOnly = true)
    public AmenityResponse obtener(
        UUID consorcioId,
        UUID amenityId,
        Authentication authentication
    ) {
        consorcioService.exigirAcceso(
            authentication,
            consorcioId
            
        );

        return AmenityResponse.desde(
            exigirAmenity(consorcioId, amenityId)
        );
    }

    @Transactional
    public AmenityResponse crear(
        UUID consorcioId,
        AmenityRequest request,
        Authentication authentication
    ) {
        consorcioService.exigirAcceso(
            authentication,
            consorcioId
            
        );

        Consorcio consorcio = consorcioRepository
            .findById(consorcioId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Consorcio no encontrado"
            ));

        String nombre = normalizarNombre(request.nombre());

        if (
            amenityRepository
                .existsByConsorcioIdAndNombreIgnoreCase(
                    consorcioId,
                    nombre
                )
        ) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Ya existe un amenity con ese nombre"
            );
        }

        Amenity amenity = new Amenity();
        amenity.setConsorcio(consorcio);
        amenity.setNombre(nombre);
        amenity.setDescripcion(
            normalizarDescripcion(request.descripcion())
        );
        amenity.setActivo(request.activo());

        return AmenityResponse.desde(
            amenityRepository.save(amenity)
        );
    }

    @Transactional
    public AmenityResponse actualizar(
        UUID consorcioId,
        UUID amenityId,
        AmenityRequest request,
        Authentication authentication
    ) {
        consorcioService.exigirAcceso(
            authentication,
            consorcioId
            
        );

        Amenity amenity = exigirAmenity(
            consorcioId,
            amenityId
        );

        String nombre = normalizarNombre(request.nombre());

        boolean nombreOcupado = amenityRepository
            .findByConsorcioIdOrderByNombre(consorcioId)
            .stream()
            .anyMatch(otro ->
                !otro.getId().equals(amenityId)
                    && otro.getNombre().equalsIgnoreCase(nombre)
            );

        if (nombreOcupado) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Ya existe otro amenity con ese nombre"
            );
        }

        amenity.setNombre(nombre);
        amenity.setDescripcion(
            normalizarDescripcion(request.descripcion())
        );
        amenity.setActivo(request.activo());

        return AmenityResponse.desde(
            amenityRepository.save(amenity)
        );
    }

    @Transactional
    public AmenityResponse cambiarEstado(
        UUID consorcioId,
        UUID amenityId,
        boolean activo,
        Authentication authentication
    ) {
        consorcioService.exigirAcceso(
            authentication,
            consorcioId
            
        );

        Amenity amenity = exigirAmenity(
            consorcioId,
            amenityId
        );

        amenity.setActivo(activo);

        return AmenityResponse.desde(
            amenityRepository.save(amenity)
        );
    }

    @Transactional(readOnly = true)
    public Amenity exigirAmenity(
        UUID consorcioId,
        UUID amenityId
    ) {
        return amenityRepository
            .findByIdAndConsorcioId(amenityId, consorcioId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Amenity no encontrado"
            ));
    }

    private String normalizarNombre(String nombre) {
        return nombre.trim();
    }

    private String normalizarDescripcion(
        String descripcion
    ) {
        if (descripcion == null) {
            return null;
        }

        String valor = descripcion.trim();

        return valor.isEmpty() ? null : valor;
    }
}