package com.example.Gericare.Service;

import com.example.Gericare.DTO.EstadisticaActividadDTO;
import com.example.Gericare.DTO.EstadisticaCuidadorDTO;
import java.io.IOException;
import java.util.List;

public interface EstadisticaService {

    List<EstadisticaCuidadorDTO> obtenerEstadisticasCuidadores();

    // Este daba error porque faltaba el DTO
    List<EstadisticaActividadDTO> obtenerEstadisticasActividadesCompletadas();

    byte[] generarReportePdf() throws IOException;

    byte[] generarReporteActividadesPdf() throws IOException;
}