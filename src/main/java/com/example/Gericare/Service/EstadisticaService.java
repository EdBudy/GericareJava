package com.example.Gericare.Service;

import com.example.Gericare.DTO.EstadisticaCuidadorDTO;
import java.io.IOException;
import java.util.List;

public interface EstadisticaService {

    // Obtener la lista de estadísticas de pacientes por cuidador
    List<EstadisticaCuidadorDTO> obtenerEstadisticasCuidadores();

    // Generar archivo PDF
    byte[] generarReportePdf() throws IOException;
}