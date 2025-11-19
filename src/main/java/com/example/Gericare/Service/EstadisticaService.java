package com.example.Gericare.Service;

import com.example.Gericare.DTO.EstadisticaActividadDTO;
import com.example.Gericare.DTO.EstadisticaCuidadorDTO;
import java.io.IOException;
import java.util.List;

public interface EstadisticaService {

    // Obtener la lista de estadísticas de pacientes por cuidador
    List<EstadisticaCuidadorDTO> obtenerEstadisticasCuidadores();

    // NUEVO: Obtener estadísticas de actividades completadas
    List<EstadisticaActividadDTO> obtenerEstadisticasActividadesCompletadas();

    // Generar archivo PDF
    byte[] generarReportePdf() throws IOException;
}