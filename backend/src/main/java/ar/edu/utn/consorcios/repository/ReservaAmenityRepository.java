package ar.edu.utn.consorcios.repository;

import ar.edu.utn.consorcios.entity.EstadoReservaAmenity;
import ar.edu.utn.consorcios.entity.ReservaAmenity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ReservaAmenityRepository
        extends JpaRepository<ReservaAmenity, UUID> {

    boolean existsByAmenityIdAndTurnoIdAndFechaAndEstado(
        UUID amenityId,
        UUID turnoId,
        LocalDate fecha,
        EstadoReservaAmenity estado
    );

    List<ReservaAmenity>
        findByAmenityIdAndFechaBetweenOrderByFechaAsc(
            UUID amenityId,
            LocalDate desde,
            LocalDate hasta
        );

    List<ReservaAmenity>
        findByPersonaIdOrderByFechaDesc(UUID personaId);
}