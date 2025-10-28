package com.example.Gericare.DTO;

import com.example.Gericare.Enums.EstadoSolicitud;
import com.example.Gericare.Enums.TipoSolicitud;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudDTO {
    private Long idSolicitud;
    private Long pacienteId;
    private String pacienteNombreCompleto;
    private Long familiarId;
    private String familiarNombreCompleto;
    private Long administradorId; // Null si aún no se gestiona
    private String administradorNombreCompleto;
    private TipoSolicitud tipoSolicitud;
    private String detalleOtro;
    private LocalDateTime fechaSolicitud;
    private String motivoSolicitud;
    private EstadoSolicitud estadoSolicitud;

    // Constructor simplificado para vista del familiar (no necesita info del admin)
    public SolicitudDTO(Long idSolicitud, Long pacienteId, String pacienteNombreCompleto, Long familiarId, String familiarNombreCompleto, TipoSolicitud tipoSolicitud, String detalleOtro, LocalDateTime fechaSolicitud, String motivoSolicitud, EstadoSolicitud estadoSolicitud) {
        this.idSolicitud = idSolicitud;
        this.pacienteId = pacienteId;
        this.pacienteNombreCompleto = pacienteNombreCompleto;
        this.familiarId = familiarId;
        this.familiarNombreCompleto = familiarNombreCompleto;
        this.tipoSolicitud = tipoSolicitud;
        this.detalleOtro = detalleOtro;
        this.fechaSolicitud = fechaSolicitud;
        this.motivoSolicitud = motivoSolicitud;
        this.estadoSolicitud = estadoSolicitud;
    }
}