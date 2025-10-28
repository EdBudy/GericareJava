package com.example.Gericare.Entity;

import com.example.Gericare.Enums.EstadoSolicitud;
import com.example.Gericare.Enums.TipoSolicitud;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_solicitud")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "estado_solicitud <> 'INACTIVO'") // Filtra automáticamente los inactivos en las consultas find*
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSolicitud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_familiar", nullable = false)
    private Familiar familiar; // Crea la solicitud

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_administrador") // Nullable, se asigna cuando el admin la gestiona? O siempre debe haber uno? Por ahora nullable.
    private Administrador administrador; // Quien gestiona/aprueba/rechaza

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_solicitud", nullable = false)
    private TipoSolicitud tipoSolicitud;

    @Column(name = "detalle_otro") // Campo para tipoSolicitud = "otro"
    private String detalleOtro;

    @Column(name = "fecha_solicitud", nullable = false, updatable = false)
    private LocalDateTime fechaSolicitud = LocalDateTime.now();

    @Column(name = "motivo_solicitud", nullable = false, columnDefinition = "TEXT")
    private String motivoSolicitud;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_solicitud", nullable = false)
    private EstadoSolicitud estadoSolicitud = EstadoSolicitud.Pendiente;
}