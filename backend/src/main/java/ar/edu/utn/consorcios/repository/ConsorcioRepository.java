package ar.edu.utn.consorcios.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ar.edu.utn.consorcios.entity.Consorcio;

public interface ConsorcioRepository extends JpaRepository<Consorcio, UUID> {

    /** Consorcio que administra el usuario, si tiene uno asignado. */
    @Query("select u.consorcio from Usuario u where lower(u.email) = lower(:email) and u.consorcio is not null")
    Optional<Consorcio> findAdministradoPor(@Param("email") String email);
}
