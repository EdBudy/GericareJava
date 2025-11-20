package com.example.Gericare.Impl;

import com.example.Gericare.DTO.EstadisticaActividadDTO;
import com.example.Gericare.DTO.EstadisticaCuidadorDTO;
import com.example.Gericare.Repository.ActividadRepository;
import com.example.Gericare.Repository.PacienteAsignadoRepository;
import com.example.Gericare.Service.EstadisticaService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
class EstadisticaServiceImpl implements EstadisticaService {

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
        return generarPdfGenerico("Distribución de Pacientes", obtenerEstadisticasCuidadores(), true);
    }

    @Override
    public byte[] generarReporteActividadesPdf() throws IOException {
        return generarPdfGenerico("Rendimiento de Cuidadores", obtenerEstadisticasActividadesCompletadas(), false);
    }

    // Método genérico privado para evitar duplicación de código (DRY)
    private byte[] generarPdfGenerico(String tituloReporte, List<?> datos, boolean esPacientes) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();

            // Estilos
            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Color.DARK_GRAY);
            Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);
            Font fontBody = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.BLACK);

            // Título
            Paragraph titulo = new Paragraph(tituloReporte, fontTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);
            document.add(Chunk.NEWLINE);

            // Gráfico
            JFreeChart chart = esPacientes ? crearGraficaBarras((List<EstadisticaCuidadorDTO>) datos)
                    : crearGraficaPastel((List<EstadisticaActividadDTO>) datos);
            BufferedImage chartImage = chart.createBufferedImage(500, 300);
            Image pdfImage = Image.getInstance(chartImage, null);
            pdfImage.setAlignment(Element.ALIGN_CENTER);
            document.add(pdfImage);
            document.add(Chunk.NEWLINE);

            // Tabla
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(90);
            table.setSpacingBefore(20f);

            // Encabezados
            PdfPCell cell1 = new PdfPCell(new Phrase("Cuidador", fontHeader));
            cell1.setBackgroundColor(Color.GRAY);
            cell1.setPadding(8);
            cell1.setHorizontalAlignment(Element.ALIGN_CENTER);

            PdfPCell cell2 = new PdfPCell(new Phrase(esPacientes ? "Pacientes Asignados" : "Actividades Completadas", fontHeader));
            cell2.setBackgroundColor(Color.GRAY);
            cell2.setPadding(8);
            cell2.setHorizontalAlignment(Element.ALIGN_CENTER);

            table.addCell(cell1);
            table.addCell(cell2);

            // Datos
            if (esPacientes) {
                for (EstadisticaCuidadorDTO dato : (List<EstadisticaCuidadorDTO>) datos) {
                    table.addCell(new Phrase(dato.getNombreCompleto(), fontBody));
                    PdfPCell cantCell = new PdfPCell(new Phrase(dato.getCantidadPacientes().toString(), fontBody));
                    cantCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(cantCell);
                }
            } else {
                for (EstadisticaActividadDTO dato : (List<EstadisticaActividadDTO>) datos) {
                    table.addCell(new Phrase(dato.getNombreCompleto(), fontBody));
                    PdfPCell cantCell = new PdfPCell(new Phrase(dato.getActividadesCompletadas().toString(), fontBody));
                    cantCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(cantCell);
                }
            }
            document.add(table);
            document.close();
            return out.toByteArray();
        }
    }

    private JFreeChart crearGraficaBarras(List<EstadisticaCuidadorDTO> datos) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        datos.forEach(d -> dataset.addValue(d.getCantidadPacientes(), "Pacientes", d.getNombreCuidador()));
        return ChartFactory.createBarChart("Carga Laboral", "Cuidador", "Cantidad", dataset, PlotOrientation.VERTICAL, false, true, false);
    }

    private JFreeChart crearGraficaPastel(List<EstadisticaActividadDTO> datos) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        datos.forEach(d -> dataset.setValue(d.getNombreCuidador(), d.getActividadesCompletadas()));
        return ChartFactory.createPieChart("Efectividad", dataset, true, true, false);
    }
}