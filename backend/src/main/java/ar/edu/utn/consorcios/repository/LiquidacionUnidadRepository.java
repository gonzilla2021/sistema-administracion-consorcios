
package ar.edu.utn.consorcios.repository;

import ar.edu.utn.consorcios.entity.LiquidacionUnidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LiquidacionUnidadRepository
        extends JpaRepository<LiquidacionUnidad, UUID> {

    List<LiquidacionUnidad> findByLiquidacionId(
            UUID liquidacionId
    );

    List<LiquidacionUnidad> findByConsorcioId(UUID consorcioId);

    List<LiquidacionUnidad> findByUnidadFuncionalId(
            UUID unidadFuncionalId
    );
}
