package ar.edu.utn.consorcios.repository;

import ar.edu.utn.consorcios.entity.BloqueoAmenity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface BloqueoAmenityRepository
        extends JpaRepository<BloqueoAmenity, UUID> {

    boolean existsByAmenityIdAndTurnoIdAndFechaAndActivoTrue(
        UUID amenityId,
        UUID turnoId,
        LocalDate fecha
    );

    List<BloqueoAmenity>
        findByAmenityIdAndFechaBetweenAndActivoTrueOrderByFechaAsc(
            UUID amenityId,
            LocalDate desde,
            LocalDate hasta
        );
}