package ar.edu.utn.consorcios.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Personas")
@Getter
@Setter
@NoArgsConstructor
public class Persona {

    @Id
    @GeneratedValue
    @Column(name = "Id")
    private UUID id;

    @Column(name = "Nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "Apellido", nullable = false, length = 100)
    private String apellido;

    @Column(name = "Dni", nullable = false, length = 10, unique = true)
    private String dni;

    @Column(name = "Email", length = 254)
    private String email;

    @Column(name = "Telefono", length = 30)
    private String telefono;
}
