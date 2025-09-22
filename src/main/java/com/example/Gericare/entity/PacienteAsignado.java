package com.example.Gericare.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "paciente_asignado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PacienteAsignado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAsignacion;

    @ManyToOne
    @JoinColumn(name = "id_paciente", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "id_cuidador", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario cuidador;

    @ManyToOne
    @JoinColumn(name = "id_familiar", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario familiar;
}