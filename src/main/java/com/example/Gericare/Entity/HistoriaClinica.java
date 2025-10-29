package com.example.Gericare.Entity;

import com.example.Gericare.Enums.EstadoUsuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

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
    @JoinColumn(name = "id_paciente", nullable = false, unique = true)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_administrador")
    private Administrador administrador;

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
    private EstadoUsuario estado = EstadoUsuario.Activo;

    // Relaciones inversas (mappedBy)

    @OneToMany(mappedBy = "historiaClinica", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<HistoriaClinicaCirugia> cirugias = new HashSet<>();


    @OneToMany(mappedBy = "historiaClinica", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<HistoriaClinicaMedicamento> medicamentos = new HashSet<>();

    @OneToMany(mappedBy = "historiaClinica", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<HistoriaClinicaEnfermedad> enfermedades = new HashSet<>();
}