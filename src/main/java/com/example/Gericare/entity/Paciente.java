package com.example.Gericare.entity;

import com.example.Gericare.enums.*;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "tb_paciente")
public class Paciente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paciente")
    private Integer id;

    @Column(name = "documento_identificacion", nullable = false, unique = true)
    private Integer documentoIdentificacion;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false, length = 50)
    private String apellido;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Genero genero;

    @Column(name = "contacto_emergencia", nullable = false, length = 20)
    private String contactoEmergencia;

    @Column(name = "estado_civil", nullable = false, length = 30)
    private String estadoCivil;

    @Convert(converter = TipoSangreConverter.class)
    @Column(name = "tipo_sangre", nullable = false)
    private TipoSangre tipoSangre;

    @Column(name = "seguro_medico", length = 100)
    private String seguroMedico;

    @Column(name = "numero_seguro", length = 50)
    private String numeroSeguro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPaciente estado;

    // --- Relación con Usuario (Familiar) ---

    // Relación Muchos a Uno: Muchos pacientes pueden estar asociados a un usuario familiar.
    @ManyToOne
    @JoinColumn(name = "id_usuario_familiar") // Esta es la columna FK en la tabla 'tb_paciente'
    private Usuario usuarioFamiliar; // Este es el campo que 'mappedBy' esperaba en 'Usuario.java'

    //-- Getters y Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDocumentoIdentificacion() {
        return documentoIdentificacion;
    }

    public void setDocumentoIdentificacion(Integer documentoIdentificacion) {
        this.documentoIdentificacion = documentoIdentificacion;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
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

    public Genero getGenero() {
        return genero;
    }

    public void setGenero(Genero genero) {
        this.genero = genero;
    }

    public String getContactoEmergencia() {
        return contactoEmergencia;
    }

    public void setContactoEmergencia(String contactoEmergencia) {
        this.contactoEmergencia = contactoEmergencia;
    }

    public String getEstadoCivil() {
        return estadoCivil;
    }

    public void setEstadoCivil(String estadoCivil) {
        this.estadoCivil = estadoCivil;
    }

    public TipoSangre getTipoSangre() {
        return tipoSangre;
    }

    public void setTipoSangre(TipoSangre tipoSangre) {
        this.tipoSangre = tipoSangre;
    }

    public String getSeguroMedico() {
        return seguroMedico;
    }

    public void setSeguroMedico(String seguroMedico) {
        this.seguroMedico = seguroMedico;
    }

    public String getNumeroSeguro() {
        return numeroSeguro;
    }

    public void setNumeroSeguro(String numeroSeguro) {
        this.numeroSeguro = numeroSeguro;
    }

    public EstadoPaciente getEstado() {
        return estado;
    }

    public void setEstado(EstadoPaciente estado) {
        this.estado = estado;
    }

    public Usuario getUsuarioFamiliar() {
        return usuarioFamiliar;
    }

    public void setUsuarioFamiliar(Usuario usuarioFamiliar) {
        this.usuarioFamiliar = usuarioFamiliar;
    }
}
