package com.example.Gericare.Entity;

import com.example.Gericare.Enums.EstadoUsuario;
import com.example.Gericare.Enums.TipoDocumento;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.hibernate.annotations.Where;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Where(clause = "estado = 'Activo'")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "rol_tipo", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDocumento tipoDocumento;

    @Column(nullable = false, unique = true)
    @Size(min = 5, max = 15, message = "El documento debe tener entre 5 y 15 caracteres")
    @Pattern(regexp = "[0-9]+", message = "El documento solo debe contener números")
    private String documentoIdentificacion;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(nullable = false)
    private String direccion;

    @Column(nullable = false, unique = true)
    private String correoElectronico;

    @Column(nullable = false)
    private String contrasena;

    @Enumerated(EnumType.STRING)
    private EstadoUsuario estado = EstadoUsuario.Activo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_rol", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Rol rol;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Size(min = 1, max = 3, message = "Debe registrar entre 1 y 3 teléfonos.")
    @NotEmpty(message = "Debe registrar al menos un teléfono.")
    private List<Telefono> telefonos = new ArrayList<>();


    // Campos para guardar token y fecha de expiración
    @Column(name = "reset_password_token")
    private String resetPasswordToken;

    @Column(name = "reset_password_token_expiry_date")
    private LocalDateTime resetPasswordTokenExpiryDate;
}