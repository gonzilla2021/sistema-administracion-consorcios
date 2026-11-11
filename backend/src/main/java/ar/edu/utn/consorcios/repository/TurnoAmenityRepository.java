package ar.edu.utn.consorcios.repository;

import ar.edu.utn.consorcios.entity.TurnoAmenity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TurnoAmenityRepository
        extends JpaRepository<TurnoAmenity, UUID> {

    List<TurnoAmenity> findByAmenityIdOrderByHoraInicio(
        UUID amenityId
    );

    List<TurnoAmenity>
        findByAmenityIdAndActivoTrueOrderByHoraInicio(
            UUID amenityId
        );

    Optional<TurnoAmenity> findByIdAndAmenityId(
        UUID id,
        UUID amenityId
    );
}