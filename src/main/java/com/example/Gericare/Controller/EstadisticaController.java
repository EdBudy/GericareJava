package com.example.Gericare.Controller;

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
        response.setContentType("application/pdf");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=reporte_cuidadores_" + currentDateTime + ".pdf";
        response.setHeader(headerKey, headerValue);

        byte[] pdfBytes = estadisticaService.generarReportePdf();

        response.getOutputStream().write(pdfBytes);
        response.getOutputStream().flush();
    }
}