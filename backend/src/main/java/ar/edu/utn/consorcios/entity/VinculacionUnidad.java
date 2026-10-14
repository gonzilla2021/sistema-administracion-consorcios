package ar.edu.utn.consorcios.entity;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "VinculacionesUnidad")
@Getter
@Setter
@NoArgsConstructor
public class VinculacionUnidad {

    @Id
    @GeneratedValue
    @Column(name = "Id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "UnidadFuncionalId", nullable = false)
    private UnidadFuncional unidadFuncional;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PersonaId", nullable = false)
    private Persona persona;

    @Enumerated(EnumType.STRING)
    @Column(name = "Tipo", nullable = false, length = 15)
    private TipoVinculo tipo;

    @Column(name = "FechaInicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "FechaFin")
    private LocalDate fechaFin;

    public boolean vigente() {
        LocalDate hoy = LocalDate.now();
        return !fechaInicio.isAfter(hoy) && (fechaFin == null || !fechaFin.isBefore(hoy));
    }
}
