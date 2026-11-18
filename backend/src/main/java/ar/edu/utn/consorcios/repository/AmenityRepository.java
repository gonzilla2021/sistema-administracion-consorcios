package ar.edu.utn.consorcios.repository;

import ar.edu.utn.consorcios.entity.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AmenityRepository
        extends JpaRepository<Amenity, UUID> {

    List<Amenity> findByConsorcioIdOrderByNombre(
        UUID consorcioId
    );

    List<Amenity> findByConsorcioIdInAndActivoTrueOrderByNombre(
        List<UUID> consorcioIds
    );

    Optional<Amenity> findByIdAndConsorcioId(
        UUID id,
        UUID consorcioId
    );

    boolean existsByConsorcioIdAndNombreIgnoreCase(
        UUID consorcioId,
        String nombre
    );

    boolean existsByConsorcioIdAndNombreIgnoreCaseAndIdNot(
        UUID consorcioId,
        String nombre,
        UUID id
    );
}