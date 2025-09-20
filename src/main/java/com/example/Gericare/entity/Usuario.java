package com.example.Gericare.entity;

import com.example.Gericare.enums.EstadoUsuario;
import com.example.Gericare.enums.TipoDocumento;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Set;
import java.util.HashSet;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tb_usuario")

public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    // Columnas de Información Personal

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false)
    private TipoDocumento tipoDocumento;

    @Column(name = "documento_identificacion", nullable = false, unique = true)
    private String documentoIdentificacion;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false, length = 50)
    private String apellido;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento; // LocalDate: fechas sin hora

    @Column(nullable = false, length = 250)
    private String direccion;

    // Columnas de Autenticación y Estado

    @Column(name = "correo_electronico", nullable = false, unique = true, length = 100)
    private String correoElectronico;

    @Column(name = "contraseña", nullable = false)
    private String contrasena; // lógica de hashing la manejará Spring Security

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoUsuario estado;

    // Atributos específicos por Rol (pueden ser nulos)

    @Column(name = "fecha_contratacion")
    private LocalDate fechaContratacion;

    @Column(name = "tipo_contrato", length = 50)
    private String tipoContrato;

    @Column(name = "contacto_emergencia", length = 20)
    private String contactoEmergencia;

    @Column(length = 50)
    private String parentesco;

    // Relaciones

    // m:1 muchos usuarios tienen un rol
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_rol") // Columna FK en la tabla 'tb_usuario' que apunta a 'tb_rol'
    private Rol rolUsuario; // objeto Rol

    // Relación Uno a Muchos con Paciente
    // Un usuario (familiar) puede tener varios pacientes asignados.
    // 'mappedBy' la relación es gestionada por la entidad Paciente (en su campo 'usuarioFamiliar')
    @OneToMany(mappedBy = "usuarioFamiliar")
    private Set<Paciente> pacientes = new HashSet<>();
}
