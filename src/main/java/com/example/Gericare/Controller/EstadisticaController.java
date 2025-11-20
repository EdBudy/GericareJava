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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/estadisticas")
public class EstadisticaController {

    @Autowired
    private EstadisticaService estadisticaService;

    @GetMapping("/cuidadores")
    public String verEstadisticasCuidadores(Model model) {
        List<EstadisticaCuidadorDTO> estadisticas = estadisticaService.obtenerEstadisticasCuidadores();
        model.addAttribute("estadisticas", estadisticas);
        return "estadisticas/admin-estadisticas-cuidadores";
    }

    @GetMapping("/cuidadores/pdf")
    public void descargarReportePdf(HttpServletResponse response) throws IOException {
        configurarRespuestaPdf(response, "reporte_cuidadores_");
        byte[] pdfBytes = estadisticaService.generarReportePdf();
        escribirPdf(response, pdfBytes);
    }

    // --- NUEVOS ENDPOINTS PARA ACTIVIDADES ---

    @GetMapping("/actividades")
    public String verEstadisticasActividades(Model model) {
        List<EstadisticaActividadDTO> stats = estadisticaService.obtenerEstadisticasActividadesCompletadas();
        model.addAttribute("stats", stats);

        // Datos para Chart.js (Parseo manual seguro para JS)
        String labels = stats.stream().map(s -> "'" + s.getNombreCompleto() + "'").collect(Collectors.joining(","));
        String data = stats.stream().map(s -> s.getActividadesCompletadas().toString()).collect(Collectors.joining(","));

        model.addAttribute("chartLabels", "[" + labels + "]");
        model.addAttribute("chartData", "[" + data + "]");

        return "estadisticas/admin-estadisticas-actividades";
    }

    @GetMapping("/actividades/pdf")
    public void descargarReporteActividadesPdf(HttpServletResponse response) throws IOException {
        configurarRespuestaPdf(response, "reporte_actividades_");
        byte[] pdfBytes = estadisticaService.generarReporteActividadesPdf();
        escribirPdf(response, pdfBytes);
    }

    // Métodos auxiliares privados
    private void configurarRespuestaPdf(HttpServletResponse response, String prefix) {
        response.setContentType("application/pdf");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH_mm");
        String currentDateTime = dateFormatter.format(new Date());
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=" + prefix + currentDateTime + ".pdf";
        response.setHeader(headerKey, headerValue);
    }

    private void escribirPdf(HttpServletResponse response, byte[] pdfBytes) throws IOException {
        response.getOutputStream().write(pdfBytes);
        response.getOutputStream().flush();
    }
}