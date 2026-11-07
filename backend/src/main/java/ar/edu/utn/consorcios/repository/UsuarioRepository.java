package ar.edu.utn.consorcios.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import ar.edu.utn.consorcios.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    /** Todos los usuarios con sus consorcios en una sola consulta (evita una consulta por usuario). */
    @Query("select distinct u from Usuario u left join fetch u.consorcios order by u.email")
    List<Usuario> findAllConConsorcios();

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPersonaId(UUID personaId);
}
