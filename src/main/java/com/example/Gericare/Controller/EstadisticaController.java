package com.example.Gericare.Controller;

import com.example.Gericare.DTO.EstadisticaActividadDTO;
import com.example.Gericare.DTO.EstadisticaCuidadorDTO;
import com.example.Gericare.Service.EstadisticaService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/estadisticas")
public class EstadisticaController {

    @Autowired
    private EstadisticaService estadisticaService;

    @GetMapping("/cuidadores")
    public String verEstadisticasCuidadores(Model model) {
        // Obtener DTOs desde el servicio
        List<EstadisticaCuidadorDTO> pacientesStats = estadisticaService.obtenerEstadisticasCuidadores();
        List<EstadisticaActividadDTO> actividadStats = estadisticaService.obtenerEstadisticasActividadesCompletadas();

        // Gráfico Pacientes
        List<String> labelsP = new ArrayList<>();
        List<Long> dataP = new ArrayList<>();
        for(EstadisticaCuidadorDTO d : pacientesStats) {
            labelsP.add(d.getNombreCuidador() + " " + d.getApellidoCuidador());
            dataP.add(d.getCantidadPacientes());
        }
        model.addAttribute("labelsCarga", labelsP);
        model.addAttribute("dataCarga", dataP);

        // Gráfico Actividades
        List<String> labelsA = new ArrayList<>();
        List<Long> dataTotal = new ArrayList<>();
        List<Long> dataRealizadas = new ArrayList<>();

        // Tabla Eficiencia
        List<Map<String, Object>> tablaEficiencia = new ArrayList<>();

        for(EstadisticaActividadDTO d : actividadStats) {
            // Datos Gráfico
            labelsA.add(d.getNombreCuidador() + " " + d.getApellidoCuidador());
            dataTotal.add(d.getActividadesAsignadas());
            dataRealizadas.add(d.getActividadesCompletadas());

            // Datos Tabla
            Map<String, Object> row = new HashMap<>();
            row.put("nombre", d.getNombreCuidador() + " " + d.getApellidoCuidador());
            row.put("asignadas", d.getActividadesAsignadas());
            row.put("completadas", d.getActividadesCompletadas());

            // Cálculo Porcentaje
            double ratio = d.getActividadesAsignadas() > 0
                    ? (double) d.getActividadesCompletadas() / d.getActividadesAsignadas() * 100
                    : 0.0;
            row.put("ratio", String.format("%.0f%%", ratio)); // Muestra %

            tablaEficiencia.add(row);
        }

        model.addAttribute("labelsProd", labelsA);
        model.addAttribute("dataTotal", dataTotal);
        model.addAttribute("dataRealizadas", dataRealizadas);
        model.addAttribute("tablaDesempeno", tablaEficiencia);

        return "estadisticas/admin-estadisticas-cuidadores";
    }

    // Endpoint pdf gráfico pacientes
    @GetMapping("/cuidadores/pdf")
    public void descargarReporteCuidadoresPdf(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=reporte_carga_laboral.pdf");
        byte[] pdfBytes = estadisticaService.generarReportePdf();
        response.getOutputStream().write(pdfBytes);
    }

    // Endpoint pdf gráfico actividades
    @GetMapping("/cuidadores/pdf-actividades")
    public void descargarReporteActividadesPdf(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=reporte_eficiencia.pdf");
        byte[] pdfBytes = estadisticaService.generarReporteActividadesPdf();
        response.getOutputStream().write(pdfBytes);
    }
}