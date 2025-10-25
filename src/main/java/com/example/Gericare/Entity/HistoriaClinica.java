package com.example.Gericare.Entity;

import com.example.Gericare.Enums.EstadoUsuario; // o EstadoRegistro
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_historia_clinica")
@Getter
@Setter
@NoArgsConstructor
public class HistoriaClinica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historia_clinica")
    private Long idHistoriaClinica;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_paciente", nullable = false, unique = true) // unique=true si solo puede haber una por paciente
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_administrador")
    private Administrador administrador; // Asumiendo que solo administradores la crean/modifican

    @Column(name = "estado_salud", columnDefinition = "TEXT")
    private String estadoSalud;

    @Column(columnDefinition = "TEXT")
    private String condiciones;

    @Column(name = "antecedentes_medicos", columnDefinition = "TEXT")
    private String antecedentesMedicos;

    @Column(columnDefinition = "TEXT")
    private String alergias;

    @Column(name = "dietas_especiales", columnDefinition = "TEXT")
    private String dietasEspeciales;

    @Column(name = "fecha_ultima_consulta")
    private LocalDate fechaUltimaConsulta;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoUsuario estado = EstadoUsuario.Activo; // o EstadoRegistro

    // Relaciones inversas (mappedBy)

    @OneToMany(mappedBy = "historiaClinica", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HistoriaClinicaCirugia> cirugias = new ArrayList<>();

    @OneToMany(mappedBy = "historiaClinica", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HistoriaClinicaMedicamento> medicamentos = new ArrayList<>();

    @OneToMany(mappedBy = "historiaClinica", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HistoriaClinicaEnfermedad> enfermedades = new ArrayList<>();
}