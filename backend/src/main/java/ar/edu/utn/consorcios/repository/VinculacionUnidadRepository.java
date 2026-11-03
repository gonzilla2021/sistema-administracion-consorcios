
package ar.edu.utn.consorcios.repository;

import ar.edu.utn.consorcios.entity.VinculacionUnidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VinculacionUnidadRepository
        extends JpaRepository<VinculacionUnidad, UUID> {

    // Busca las vinculaciones de una unidad funcional.
    List<VinculacionUnidad> findByUnidadFuncionalId(
            UUID unidadFuncionalId
    );

    // Busca las vinculaciones de una persona.
    List<VinculacionUnidad> findByPersonaId(
            UUID personaId
    );
}

