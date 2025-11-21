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

    // -------------------------------------------------------
    // SECCIÓN 1: ESTADÍSTICAS DE CUIDADORES (Carga y Desempeño)
    // -------------------------------------------------------
    @GetMapping("/cuidadores")
    public String verEstadisticasCuidadores(Model model) {
        // 1. Obtener datos crudos
        List<EstadisticaCuidadorDTO> pacientesPorCuidador = estadisticaService.obtenerEstadisticasCuidadores();
        List<EstadisticaActividadDTO> actividadesCompletadas = estadisticaService.obtenerEstadisticasActividadesCompletadas();

        // 2. Procesar Tabla de Desempeño (Lógica de Negocio)
        List<Map<String, Object>> tablaDesempeno = new ArrayList<>();

        for (EstadisticaCuidadorDTO pc : pacientesPorCuidador) {
            Map<String, Object> row = new HashMap<>();
            row.put("nombre", pc.getNombreCompleto());
            row.put("pacientes", pc.getCantidadPacientes());

            // Buscar actividades de este cuidador específico de forma segura
            Long actividades = actividadesCompletadas.stream()
                    .filter(a -> a.getNombreCompleto() != null && a.getNombreCompleto().equals(pc.getNombreCompleto()))
                    .map(EstadisticaActividadDTO::getActividadesCompletadas)
                    .findFirst()
                    .orElse(0L);

            row.put("actividades", actividades);

            // Calcular Eficiencia: Evitar división por cero
            double eficiencia = pc.getCantidadPacientes() > 0 ? (double) actividades / pc.getCantidadPacientes() : 0.0;
            row.put("ratio", String.format("%.2f", eficiencia)); // 2 decimales para mayor precisión

            tablaDesempeno.add(row);
        }

        model.addAttribute("tablaDesempeno", tablaDesempeno);

        // 3. Datos para Gráficas (Enviamos Listas puras, Thymeleaf las serializa a JSON)
        // Gráfica 1: Carga Laboral
        model.addAttribute("labelsCarga", pacientesPorCuidador.stream().map(EstadisticaCuidadorDTO::getNombreCompleto).collect(Collectors.toList()));
        model.addAttribute("dataCarga", pacientesPorCuidador.stream().map(EstadisticaCuidadorDTO::getCantidadPacientes).collect(Collectors.toList()));

        // Gráfica 2: Productividad comparativa
        model.addAttribute("labelsProd", actividadesCompletadas.stream().map(EstadisticaActividadDTO::getNombreCompleto).collect(Collectors.toList()));
        model.addAttribute("dataProd", actividadesCompletadas.stream().map(EstadisticaActividadDTO::getActividadesCompletadas).collect(Collectors.toList()));

        return "estadisticas/admin-estadisticas-cuidadores";
    }

    @GetMapping("/cuidadores/pdf")
    public void descargarReporteCuidadoresPdf(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        // Buen nombre de archivo con fecha implícita o contexto
        response.setHeader("Content-Disposition", "attachment; filename=reporte_desempeno_personal.pdf");
        byte[] pdfBytes = estadisticaService.generarReportePdf();
        response.getOutputStream().write(pdfBytes);
    }

    // -------------------------------------------------------
    // SECCIÓN 2: ESTADÍSTICAS DE ACTIVIDADES (Cumplimiento)
    // -------------------------------------------------------
    @GetMapping("/actividades")
    public String verEstadisticasActividades(Model model) {
        // 1. Obtener datos
        List<EstadisticaActividadDTO> stats = estadisticaService.obtenerEstadisticasActividadesCompletadas();

        // 2. Separar datos para Chart.js
        // Extraemos nombres y cantidades en listas separadas
        List<String> labels = stats.stream()
                .map(EstadisticaActividadDTO::getNombreCompleto)
                .collect(Collectors.toList());

        List<Long> data = stats.stream()
                .map(EstadisticaActividadDTO::getActividadesCompletadas)
                .collect(Collectors.toList());

        // 3. Inyectar al modelo
        model.addAttribute("stats", stats);          // Para la lista de "Líderes"
        model.addAttribute("chartLabels", labels);   // Para la dona (Doughnut chart)
        model.addAttribute("chartData", data);       // Valores numéricos

        return "estadisticas/admin-estadisticas-actividades";
    }

    @GetMapping("/actividades/pdf")
    public void descargarReporteActividadesPdf(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=reporte_actividades.pdf");
        // Asumo que tienes o implementarás este método en el servicio, si no existe,
        // puedes usar el mismo generarReportePdf() o crear uno nuevo.
        byte[] pdfBytes = estadisticaService.generarReporteActividadesPdf();
        response.getOutputStream().write(pdfBytes);
    }
}