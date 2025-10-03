package com.example.Gericare.entity;

import com.example.Gericare.enums.EstadoAsignacion;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;
import java.time.LocalDateTime;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_paciente", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cuidador", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Cuidador cuidador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_familiar") // Puede ser nulo
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Familiar familiar;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoAsignacion estado = EstadoAsignacion.Activo; // Borrado lógico

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now(); // Registro de cuándo se creó

    // Guardar quién realizó la asignación (admin).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_admin_creador", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Administrador adminCreador;
}