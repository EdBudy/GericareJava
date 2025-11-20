package com.example.Gericare.Service;

import com.example.Gericare.DTO.EstadisticaActividadDTO;
import com.example.Gericare.DTO.EstadisticaCuidadorDTO;
import java.io.IOException;
import java.util.List;

public interface EstadisticaService {
    List<EstadisticaCuidadorDTO> obtenerEstadisticasCuidadores();
    List<EstadisticaActividadDTO> obtenerEstadisticasActividadesCompletadas();
    byte[] generarReportePdf() throws IOException; // Reporte Pacientes
    byte[] generarReporteActividadesPdf() throws IOException; // NUEVO: Reporte Rendimiento
}