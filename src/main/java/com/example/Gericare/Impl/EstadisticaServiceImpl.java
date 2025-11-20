package com.example.Gericare.Impl;

import com.example.Gericare.DTO.EstadisticaActividadDTO;
import com.example.Gericare.DTO.EstadisticaCuidadorDTO;
import com.example.Gericare.Repository.ActividadRepository;
import com.example.Gericare.Repository.PacienteAsignadoRepository;
import com.example.Gericare.Service.EstadisticaService;

// Imports de OpenPDF
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.jfree.chart.*;
// Imports de JFreeChart
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
        return generarPdfGenerico("Pacientes por Cuidador", datos, null);
    }

    @Override
    public byte[] generarReporteActividadesPdf() throws IOException {
        List<EstadisticaActividadDTO> datos = obtenerEstadisticasActividadesCompletadas();
        return generarPdfGenerico("Rendimiento Actividades", null, datos);
    }

    private byte[] generarPdfGenerico(String tituloReporte,
                                      List<EstadisticaCuidadorDTO> datosPacientes,
                                      List<EstadisticaActividadDTO> datosActividades) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();

            Color colorTitulo = new Color(0, 123, 255);
            Color colorGris = new Color(128, 128, 128);
            Color colorHeader = new Color(33, 37, 41);

            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, colorTitulo);
            Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA, 12, colorGris);
            Font fontHeaderTabla = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);
            Font fontData = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.BLACK);

            Paragraph titulo = new Paragraph(tituloReporte, fontTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);

            Paragraph subtitulo = new Paragraph("Reporte Generado por Gericare Connect", fontSubtitulo);
            subtitulo.setAlignment(Element.ALIGN_CENTER);
            subtitulo.setSpacingAfter(20);
            document.add(subtitulo);
            document.add(Chunk.NEWLINE);

            JFreeChart chart = null;
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(20f);

            PdfPCell cellHeader1 = new PdfPCell(new Phrase("Nombre", fontHeaderTabla));
            PdfPCell cellHeader2 = new PdfPCell(new Phrase("Cantidad", fontHeaderTabla));

            cellHeader1.setBackgroundColor(colorHeader);
            cellHeader2.setBackgroundColor(colorHeader);
            cellHeader1.setPadding(10);
            cellHeader2.setPadding(10);
            cellHeader1.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellHeader2.setHorizontalAlignment(Element.ALIGN_CENTER);

            table.addCell(cellHeader1);
            table.addCell(cellHeader2);

            if (datosPacientes != null) {
                chart = crearGraficaBarras(datosPacientes);
                for (EstadisticaCuidadorDTO d : datosPacientes) {
                    addCell(table, d.getNombreCompleto(), fontData);
                    addCell(table, d.getCantidadPacientes().toString(), fontData);
                }
            } else if (datosActividades != null) {
                chart = crearGraficaTorta(datosActividades);
                for (EstadisticaActividadDTO d : datosActividades) {
                    addCell(table, d.getNombreCompleto(), fontData);
                    addCell(table, d.getActividadesCompletadas().toString(), fontData);
                }
            }

            if (chart != null) {
                chart.setBackgroundPaint(Color.WHITE);
                BufferedImage chartImage = chart.createBufferedImage(500, 300);
                Image pdfImage = Image.getInstance(chartImage, null);
                pdfImage.setAlignment(Element.ALIGN_CENTER);
                document.add(pdfImage);
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        }
    }

    private void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(8);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private JFreeChart crearGraficaBarras(List<EstadisticaCuidadorDTO> datos) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (EstadisticaCuidadorDTO dato : datos) {
            dataset.addValue(dato.getCantidadPacientes(), "Pacientes", dato.getNombreCuidador());
        }
        return ChartFactory.createBarChart("", "Cuidador", "Pacientes", dataset, PlotOrientation.VERTICAL, false, true, false);
    }

    private JFreeChart crearGraficaTorta(List<EstadisticaActividadDTO> datos) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (EstadisticaActividadDTO dato : datos) {
            dataset.setValue(dato.getNombreCompleto(), dato.getActividadesCompletadas());
        }
        return ChartFactory.createPieChart("", dataset, true, true, false);
    }
}