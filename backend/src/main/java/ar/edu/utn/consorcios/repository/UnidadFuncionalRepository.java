package ar.edu.utn.consorcios.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import ar.edu.utn.consorcios.entity.UnidadFuncional;

public interface UnidadFuncionalRepository extends JpaRepository<UnidadFuncional, UUID> {

    List<UnidadFuncional> findByConsorcioIdOrderByIdentificador(UUID consorcioId);

    long countByConsorcioId(UUID consorcioId);

    /** Pares (consorcioId, cantidad) de todos los consorcios que tienen unidades. */
    @Query("select u.consorcio.id, count(u) from UnidadFuncional u group by u.consorcio.id")
    List<Object[]> contarPorConsorcio();
}
