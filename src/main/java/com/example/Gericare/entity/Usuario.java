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
    private Integer id;

    // Columnas de Información Personal

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false)
    private TipoDocumento tipoDocumento;

    @Column(name = "documento_identificacion", nullable = false, unique = true)
    private Integer documentoIdentificacion;

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

    // Definición de Relaciones con otras Entidades

    // Relación Muchos a Muchos con Rol a través de una tabla intermedia
    @ManyToMany(fetch = FetchType.EAGER) // EAGER para que los roles se carguen junto con el usuario
    @JoinTable(
            name = "tb_usuario_rol", // nombre tabla intermedia
            joinColumns = @JoinColumn(name = "id_usuario"), // columna que conecta en la tabla intermedia
            inverseJoinColumns = @JoinColumn(name = "id_rol") // columna que conecta a la entidad Rol
    )
    private Set<Rol> roles = new HashSet<>();

    // Relación Uno a Muchos con Paciente
    // Un usuario (familiar) puede tener varios pacientes asignados.
    // 'mappedBy' la relación es gestionada por la entidad Paciente (en su campo 'usuarioFamiliar')
    @OneToMany(mappedBy = "usuarioFamiliar")
    private Set<Paciente> pacientes = new HashSet<>();

    //-- Getters y Setters


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public TipoDocumento getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(TipoDocumento tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public Integer getDocumentoIdentificacion() {
        return documentoIdentificacion;
    }

    public void setDocumentoIdentificacion(Integer documentoIdentificacion) {
        this.documentoIdentificacion = documentoIdentificacion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public EstadoUsuario getEstado() {
        return estado;
    }

    public void setEstado(EstadoUsuario estado) {
        this.estado = estado;
    }

    public LocalDate getFechaContratacion() {
        return fechaContratacion;
    }

    public void setFechaContratacion(LocalDate fechaContratacion) {
        this.fechaContratacion = fechaContratacion;
    }

    public String getTipoContrato() {
        return tipoContrato;
    }

    public void setTipoContrato(String tipoContrato) {
        this.tipoContrato = tipoContrato;
    }

    public String getContactoEmergencia() {
        return contactoEmergencia;
    }

    public void setContactoEmergencia(String contactoEmergencia) {
        this.contactoEmergencia = contactoEmergencia;
    }

    public String getParentesco() {
        return parentesco;
    }

    public void setParentesco(String parentesco) {
        this.parentesco = parentesco;
    }

    public Set<Rol> getRoles() {
        return roles;
    }

    public void setRoles(Set<Rol> roles) {
        this.roles = roles;
    }

    public Set<Paciente> getPacientes() {
        return pacientes;
    }

    public void setPacientes(Set<Paciente> pacientes) {
        this.pacientes = pacientes;
    }
}
