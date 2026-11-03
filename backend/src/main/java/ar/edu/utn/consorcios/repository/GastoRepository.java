
package ar.edu.utn.consorcios.repository;

import ar.edu.utn.consorcios.entity.Gasto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface GastoRepository extends JpaRepository<Gasto, UUID> {

    // Busca todos los gastos de un consorcio.
    List<Gasto> findByConsorcioId(UUID consorcioId);

    // Busca los gastos de un consorcio entre dos fechas.
    List<Gasto> findByConsorcioIdAndFechaBetween(
            UUID consorcioId,
            LocalDate desde,
            LocalDate hasta
    );
}
