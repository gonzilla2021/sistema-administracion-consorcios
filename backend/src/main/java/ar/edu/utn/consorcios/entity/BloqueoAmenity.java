package ar.edu.utn.consorcios.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "bloqueos_amenity",
    schema = "dbo",
    indexes = {
        @Index(
            name = "idx_bloqueo_amenity_fecha",
            columnList = "amenity_id,fecha"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
public class BloqueoAmenity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "amenity_id", nullable = false)
    private Amenity amenity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "turno_id", nullable = false)
    private TurnoAmenity turno;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, length = 500)
    private String motivo;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private boolean activo = true;
}