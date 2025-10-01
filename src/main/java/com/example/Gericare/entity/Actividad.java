package com.example.Gericare.entity;

import com.example.Gericare.enums.EstadoActividad;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "tb_actividad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Actividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_actividad")
    private Long idActividad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_administrador", nullable = false)
    private Administrador administrador;

    @Column(name = "tipo_actividad", nullable = false, length = 100)
    private String tipoActividad;

    @Column(name = "descripcion_actividad", columnDefinition = "TEXT")
    private String descripcionActividad;

    @Column(name = "fecha_actividad", nullable = false)
    private LocalDate fechaActividad;

    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "hora_fin")
    private LocalTime horaFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_actividad", nullable = false)
    private EstadoActividad estadoActividad;
}
