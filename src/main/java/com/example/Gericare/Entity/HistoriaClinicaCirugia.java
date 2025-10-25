package com.example.Gericare.Entity;

import com.example.Gericare.Enums.EstadoUsuario; // o EstadoRegistro
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "tb_historia_clinica_cirugia")
@Getter
@Setter
@NoArgsConstructor
public class HistoriaClinicaCirugia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cirugia")
    private Long idCirugia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_historia_clinica", nullable = false)
    private HistoriaClinica historiaClinica;

    @Column(name = "descripcion_cirugia", nullable = false, length = 250)
    private String descripcionCirugia;

    @Column(name = "fecha_cirugia")
    private LocalDate fechaCirugia;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoUsuario estado = EstadoUsuario.Activo; // o EstadoRegistro
}