
package ar.edu.utn.consorcios.repository;

import ar.edu.utn.consorcios.entity.Liquidacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LiquidacionRepository
        extends JpaRepository<Liquidacion, UUID> {

    // Busca la liquidación de un consorcio y período.
    Optional<Liquidacion> findByConsorcioIdAndPeriodo(
            UUID consorcioId,
            String periodo
    );
}
