package ar.edu.utn.consorcios.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

// Persona registrada por el administrador dentro de un consorcio.
// El DNI es único dentro del consorcio.
@Entity
@Table(
        name = "Personas",
        uniqueConstraints = @UniqueConstraint(
                name = "UQ_Personas_Consorcio_Dni",
                columnNames = {"ConsorcioId", "Dni"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class Persona {

    @Id
    @GeneratedValue
    @Column(name = "Id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ConsorcioId", nullable = false)
    private Consorcio consorcio;

    @Column(name = "Nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "Apellido", nullable = false, length = 100)
    private String apellido;

    @Column(name = "Dni", nullable = false, length = 10)
    private String dni;

    @Column(name = "Email", length = 254)
    private String email;

    @Column(name = "Telefono", length = 30)
    private String telefono;
}
