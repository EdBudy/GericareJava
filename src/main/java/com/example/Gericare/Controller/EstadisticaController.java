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
    public String verEstadisticasUnificadas(Model model) {
        // 1. Obtener Pacientes por Cuidador
        List<EstadisticaCuidadorDTO> pacientesPorCuidador = estadisticaService.obtenerEstadisticasCuidadores();

        // 2. Obtener Actividades Completadas por Cuidador
        List<EstadisticaActividadDTO> actividadesCompletadas = estadisticaService.obtenerEstadisticasActividadesCompletadas();

        // 3. FUSIONAR DATOS PARA LA TABLA DE DESEMPEÑO
        // Creamos una estructura combinada para la vista
        List<Map<String, Object>> tablaDesempeno = new ArrayList<>();

        for (EstadisticaCuidadorDTO pc : pacientesPorCuidador) {
            Map<String, Object> row = new HashMap<>();
            row.put("nombre", pc.getNombreCompleto());
            row.put("pacientes", pc.getCantidadPacientes());

            // Buscar actividades de este cuidador
            Long actividades = actividadesCompletadas.stream()
                    .filter(a -> a.getNombreCompleto().equals(pc.getNombreCompleto()))
                    .map(EstadisticaActividadDTO::getActividadesCompletadas)
                    .findFirst()
                    .orElse(0L);

            row.put("actividades", actividades);

            // Calcular Eficiencia (Actividades / Pacientes) * Factor (Simple)
            double eficiencia = pc.getCantidadPacientes() > 0 ? (double) actividades / pc.getCantidadPacientes() : 0;
            row.put("ratio", String.format("%.1f", eficiencia)); // Promedio act por paciente

            tablaDesempeno.add(row);
        }

        model.addAttribute("tablaDesempeno", tablaDesempeno);

        // Datos para Grafica 1 (Carga Laboral)
        model.addAttribute("labelsCarga", pacientesPorCuidador.stream().map(p -> "'" + p.getNombreCompleto() + "'").collect(Collectors.toList()));
        model.addAttribute("dataCarga", pacientesPorCuidador.stream().map(EstadisticaCuidadorDTO::getCantidadPacientes).collect(Collectors.toList()));

        // Datos para Grafica 2 (Productividad)
        model.addAttribute("labelsProd", actividadesCompletadas.stream().map(a -> "'" + a.getNombreCompleto() + "'").collect(Collectors.toList()));
        model.addAttribute("dataProd", actividadesCompletadas.stream().map(EstadisticaActividadDTO::getActividadesCompletadas).collect(Collectors.toList()));

        return "estadisticas/admin-estadisticas-cuidadores";
    }

    @GetMapping("/cuidadores/pdf")
    public void descargarReportePdf(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=reporte_general.pdf");
        byte[] pdfBytes = estadisticaService.generarReportePdf();
        response.getOutputStream().write(pdfBytes);
    }
}