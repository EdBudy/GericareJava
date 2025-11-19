package com.example.Gericare.Impl;

import com.example.Gericare.DTO.EstadisticaActividadDTO;
import com.example.Gericare.DTO.EstadisticaCuidadorDTO;
import com.example.Gericare.Repository.ActividadRepository;
import com.example.Gericare.Repository.PacienteAsignadoRepository;
import com.example.Gericare.Service.EstadisticaService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class EstadisticaServiceImpl implements EstadisticaService {

    @Autowired
    private PacienteAsignadoRepository pacienteAsignadoRepository;

    @Autowired
    private ActividadRepository actividadRepository;

    @Override
    public List<EstadisticaCuidadorDTO> obtenerEstadisticasCuidadores() {
        return pacienteAsignadoRepository.obtenerPacientesPorCuidador();
    }

    @Override
    public List<EstadisticaActividadDTO> obtenerEstadisticasActividadesCompletadas() {
        return actividadRepository.countActividadesCompletadasPorCuidador();
    }

    @Override
    public byte[] generarReportePdf() throws IOException {
        List<EstadisticaCuidadorDTO> datos = obtenerEstadisticasCuidadores();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);

            document.open();

            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph titulo = new Paragraph("Reporte de Distribución de Pacientes", fontTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);
            document.add(Chunk.NEWLINE);

            Font fontCuerpo = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Paragraph descripcion = new Paragraph(
                    "A continuación se presenta la distribución actual de pacientes asignados a los cuidadores activos en el sistema.", fontCuerpo);
            descripcion.setAlignment(Element.ALIGN_JUSTIFIED);
            document.add(descripcion);
            document.add(Chunk.NEWLINE);

            JFreeChart chart = crearGraficaBarras(datos);
            BufferedImage chartImage = chart.createBufferedImage(500, 350);

            Image pdfImage = Image.getInstance(chartImage, null);
            pdfImage.setAlignment(Element.ALIGN_CENTER);
            document.add(pdfImage);
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(80);
            table.addCell(new Phrase("Cuidador", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            table.addCell(new Phrase("Total Pacientes", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));

            for (EstadisticaCuidadorDTO dato : datos) {
                table.addCell(dato.getNombreCompleto());
                table.addCell(dato.getCantidadPacientes().toString());
            }
            document.add(table);

            document.close();
            return out.toByteArray();
        }
    }

    private JFreeChart crearGraficaBarras(List<EstadisticaCuidadorDTO> datos) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (EstadisticaCuidadorDTO dato : datos) {
            dataset.addValue(dato.getCantidadPacientes(), "Pacientes", dato.getNombreCuidador());
        }

        return ChartFactory.createBarChart(
                "Pacientes por Cuidador",
                "Cuidador",
                "Cantidad",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );
    }
}