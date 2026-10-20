package ar.edu.utn.consorcios.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.utn.consorcios.entity.Persona;

public interface PersonaRepository extends JpaRepository<Persona, UUID> {
}
