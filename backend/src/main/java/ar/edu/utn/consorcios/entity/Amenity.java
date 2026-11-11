package ar.edu.utn.consorcios.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
    name = "amenities",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_amenity_consorcio_nombre",
            columnNames = {"consorcio_id", "nombre"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
public class Amenity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "consorcio_id", nullable = false)
    private Consorcio consorcio;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false)
    private boolean activo = true;
}
