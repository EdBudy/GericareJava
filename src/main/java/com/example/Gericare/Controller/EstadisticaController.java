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

    // inyecta el servicio que contiene la lógica de negocio
    @Autowired
    private EstadisticaService estadisticaService;
    
    // muestra la página con las estadísticas de cuidadores
    @GetMapping("/cuidadores")
    public String verEstadisticasCuidadores(Model model) {
        // obtiene los datos de cuántos pacientes tiene cada cuidador
        // llama al servicio que a la vez consulta la bd
        List<EstadisticaCuidadorDTO> pacientesStats = estadisticaService.obtenerEstadisticasCuidadores();

        // obtiene datos de actividades completadas por cada cuidador
        // (del día actual porque el servicio filtra por fecha de hoy)
        List<EstadisticaActividadDTO> actividadStats = estadisticaService.obtenerEstadisticasActividadesCompletadas();

        // prepara datos para el primer gráfico (pacientes por cuidador)
        // lista para los nombres de los cuidadores (etiquetas del gráfico)
        List<String> labelsP = new ArrayList<>();
        // lista para la cantidad de pacientes (datos del gráfico)
        List<Long> dataP = new ArrayList<>();
        // recorre cada dto de estadísticas de cuidadores
        for(EstadisticaCuidadorDTO d : pacientesStats) {
            labelsP.add(d.getNombreCuidador() + " " + d.getApellidoCuidador());
            // obtiene la cantidad de pacientes de ese cuidador
            dataP.add(d.getCantidadPacientes());
        }

        // agrega las listas al modelo para que thymeleaf las use en la vista
        model.addAttribute("labelsCarga", labelsP);
        model.addAttribute("dataCarga", dataP);

        // prepara los datos para el segundo gráfico (actividades por cuidador)
        // lista para nombres de cuidadores en el gráfico de actividades
        List<String> labelsA = new ArrayList<>();
        // lista para el total de actividades asignadas a cada cuidador (hoy)
        List<Long> dataTotal = new ArrayList<>();
        // lista para actividades completadas por cada cuidador (hoy)
        List<Long> dataRealizadas = new ArrayList<>();

        // prepara los datos para la tabla de eficiencia que se muestra abajo
        // (cada fila de la tabla es un mapa con clave-valor)
        List<Map<String, Object>> tablaEficiencia = new ArrayList<>();

        // recorre cada dto de estadísticas de actividades
        for(EstadisticaActividadDTO d : actividadStats) {
            // datos para el gráfico de actividades
            // etiqueta (nombre completo del cuidador)
            labelsA.add(d.getNombreCuidador() + " " + d.getApellidoCuidador());
            // dato: total de actividades asignadas hoy
            dataTotal.add(d.getActividadesAsignadas());
            // dato: actividades completadas hoy
            dataRealizadas.add(d.getActividadesCompletadas());


            // prepara una fila para la tabla de eficiencia
            Map<String, Object> row = new HashMap<>();
            // clave "nombre" con valor nombre completo
            row.put("nombre", d.getNombreCuidador() + " " + d.getApellidoCuidador());
            // clave "asignadas" con valor actividades asignadas
            row.put("asignadas", d.getActividadesAsignadas());
            // clave "completadas" con valor actividades completadas
            row.put("completadas", d.getActividadesCompletadas());

            // calcula el porcentaje de eficiencia
            // si hay actividades asignadas, calcula (completadas/asignadas)*100
            // si no hay actividades asignadas, ratio = 0
            double ratio = d.getActividadesAsignadas() > 0
                    ? (double) d.getActividadesCompletadas() / d.getActividadesAsignadas() * 100
                    : 0.0;
            // formatea el ratio como porcentaje sin decimales
            row.put("ratio", String.format("%.0f%%", ratio));

            // agrega esta fila a la lista de la tabla
            tablaEficiencia.add(row);
        }

        // agrega todos los datos del gráfico de actividades al modelo
        model.addAttribute("labelsProd", labelsA);
        model.addAttribute("dataTotal", dataTotal);
        model.addAttribute("dataRealizadas", dataRealizadas);
        // agrega la tabla de eficiencia al modelo
        model.addAttribute("tablaDesempeno", tablaEficiencia);

        // retorna la plantilla thymeleaf que se va a mostrar
        return "estadisticas/admin-estadisticas-cuidadores";
    }

    // descarga del pdf de carga laboral (pacientes por cuidador)
    // responde con un archivo pdf que se descarga automáticamente
    @GetMapping("/cuidadores/pdf")
    public void descargarReporteCuidadoresPdf(HttpServletResponse response) throws IOException {
        // indica que la respuesta es un archivo pdf
        response.setContentType("application/pdf");
        // indica que el navegador debe descargar el archivo con este nombre
        response.setHeader("Content-Disposition", "attachment; filename=reporte_carga_laboral.pdf");
        // llama al servicio para generar el pdf (datos totales)
        // el servicio devuelve el pdf como un array de bytes
        byte[] pdfBytes = estadisticaService.generarReportePdf();
        // escribe los bytes del pdf en el outputstream de la respuesta
        // el navegador recibe esto y lo descarga como archivo
        response.getOutputStream().write(pdfBytes);
    }

    // descarga del pdf de eficiencia (actividades por cuidador)
    @GetMapping("/cuidadores/pdf-actividades")
    public void descargarReporteActividadesPdf(HttpServletResponse response) throws IOException {
        // indica que es un pdf
        response.setContentType("application/pdf");
        // nombre del archivo a descargar
        response.setHeader("Content-Disposition", "attachment; filename=reporte_eficiencia.pdf");
        // genera el pdf de actividades (datos totales)
        byte[] pdfBytes = estadisticaService.generarReporteActividadesPdf();
        // escribe los bytes en la respuesta
        response.getOutputStream().write(pdfBytes);
    }
}