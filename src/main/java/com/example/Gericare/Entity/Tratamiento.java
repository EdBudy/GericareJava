package com.example.Gericare.Entity;

import com.example.Gericare.Enums.EstadoActividad; // Reutiliza EstadoActividad
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDate;

@Entity
@Table(name = "tb_tratamiento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "estado_tratamiento <> 'Inactivo'")
public class Tratamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTratamiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_administrador", nullable = false)
    private Administrador administrador; // Crea tratamiento

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_cuidador", nullable = false)
    private Cuidador cuidador; // Ejecuta tratamiento

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "instrucciones_especiales", columnDefinition = "TEXT")
    private String instruccionesEspeciales;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_tratamiento", nullable = false)
    private EstadoActividad estadoTratamiento = EstadoActividad.Pendiente;
}