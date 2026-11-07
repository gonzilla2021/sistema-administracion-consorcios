package ar.edu.utn.consorcios.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Usuarios")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue
    @Column(name = "Id")
    private UUID id;

    @Column(name = "Email", nullable = false, length = 254, unique = true)
    private String email;

    @Column(name = "PasswordHash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "Rol", nullable = false, length = 30)
    private Rol rol;

    @Column(name = "Activo", nullable = false)
    private boolean activo = true;

    @Column(name = "FechaAlta", nullable = false)
    private LocalDateTime fechaAlta = LocalDateTime.now();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PersonaId")
    private Persona persona;

    /** Consorcios que administra. Solo aplica a ADMINISTRADOR_CONSORCIO; puede tener varios o ninguno. */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "UsuariosConsorcios",
            joinColumns = @JoinColumn(name = "UsuarioId"),
            inverseJoinColumns = @JoinColumn(name = "ConsorcioId"))
    private Set<Consorcio> consorcios = new HashSet<>();
}
