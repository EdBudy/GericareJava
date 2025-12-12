package com.example.Gericare.Service;

import com.example.Gericare.DTO.EstadisticaActividadDTO;
import com.example.Gericare.DTO.EstadisticaCuidadorDTO;
import java.io.IOException;
import java.util.List;

// define métodos que debe implementar el servicio para manejar estadísticas
public interface EstadisticaService {

    // obtiene cantidad de pacientes por cuidador (activos)
    List<EstadisticaCuidadorDTO> obtenerEstadisticasCuidadores();

    // obtiene estadísticas de actividades completadas por cuidador
    List<EstadisticaActividadDTO> obtenerEstadisticasActividadesCompletadas();

    // genera pdf con el reporte de carga laboral (pacientes por cuidador, datos totales)
    byte[] generarReportePdf() throws IOException;

    // genera pdf con el reporte de eficiencia (actividades por cuidado, datos totales)
    byte[] generarReporteActividadesPdf() throws IOException;
}