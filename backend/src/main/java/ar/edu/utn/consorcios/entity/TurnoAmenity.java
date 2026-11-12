package ar.edu.utn.consorcios.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(
    name = "turnos_amenity",
    schema = "dbo",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_turno_amenity_nombre",
            columnNames = {"amenity_id", "nombre"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
public class TurnoAmenity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "amenity_id", nullable = false)
    private Amenity amenity;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(nullable = false)
    private boolean activo = true;
}