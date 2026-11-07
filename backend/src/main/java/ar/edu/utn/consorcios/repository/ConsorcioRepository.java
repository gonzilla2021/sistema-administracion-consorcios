package ar.edu.utn.consorcios.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ar.edu.utn.consorcios.entity.Consorcio;

public interface ConsorcioRepository extends JpaRepository<Consorcio, UUID> {

    /** Consorcios que administra el usuario. */
    @Query("select c from Usuario u join u.consorcios c where lower(u.email) = lower(:email) order by c.nombre")
    List<Consorcio> findAdministradosPor(@Param("email") String email);

    /** El consorcio indicado, solo si el usuario lo administra (una sola consulta). */
    @Query("select c from Usuario u join u.consorcios c where lower(u.email) = lower(:email) and c.id = :id")
    Optional<Consorcio> findAdministradoPor(@Param("email") String email, @Param("id") UUID id);
}
