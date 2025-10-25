package com.example.Gericare.Entity;

import com.example.Gericare.Enums.EstadoUsuario; // o EstadoRegistro
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "tb_historia_clinica_enfermedad")
@Getter
@Setter
@NoArgsConstructor
public class HistoriaClinicaEnfermedad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_hc_enfermedad")
    private Long idHcEnfermedad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_historia_clinica", nullable = false)
    private HistoriaClinica historiaClinica;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_enfermedad", nullable = false)
    private Enfermedad enfermedad;

    @Column(name = "fecha_diagnostico")
    private LocalDate fechaDiagnostico;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoUsuario estado = EstadoUsuario.Activo; // o EstadoRegistro
}