package com.example.Gericare.Impl;

import com.example.Gericare.DTO.EstadisticaActividadDTO;
import com.example.Gericare.DTO.EstadisticaCuidadorDTO;
import com.example.Gericare.Entity.Cuidador;
import com.example.Gericare.Entity.Usuario;
import com.example.Gericare.Enums.EstadoActividad;
import com.example.Gericare.Enums.RolNombre;
import com.example.Gericare.Repository.ActividadRepository;
import com.example.Gericare.Repository.PacienteAsignadoRepository;
import com.example.Gericare.Repository.UsuarioRepository;
import com.example.Gericare.Service.EstadisticaService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class EstadisticaServiceImpl implements EstadisticaService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PacienteAsignadoRepository pacienteAsignadoRepository;
    @Autowired
    private ActividadRepository actividadRepository;

    @Override
    public List<EstadisticaCuidadorDTO> obtenerEstadisticasCuidadores() {
        // Usa el método del Repositorio
        return pacienteAsignadoRepository.obtenerPacientesPorCuidador();
    }

    @Override
    public List<EstadisticaActividadDTO> obtenerEstadisticasActividadesCompletadas() {
        List<EstadisticaActividadDTO> dtos = new ArrayList<>();

        // Busca usuario con rol Cuidador
        List<Usuario> cuidadores = usuarioRepository.findByRol_RolNombre(RolNombre.Cuidador);

        for (Usuario u : cuidadores) {
            if (u instanceof Cuidador) {
                Cuidador cuidador = (Cuidador) u;

                // Usa queries personalizadas que se agregan al ActividadRepository
                Long total = actividadRepository.countTotalActividadesAsignadas(cuidador);
                Long completadas = actividadRepository.countActividadesByCuidadorAndEstado(cuidador, EstadoActividad.Completada);

                dtos.add(new EstadisticaActividadDTO(
                        cuidador.getNombre(),
                        cuidador.getApellido(),
                        total,
                        completadas
                ));
            }
        }
        return dtos;
    }

    // Generación pdfs

    @Override
    public byte[] generarReportePdf() throws IOException {
        // Gráfico Pacientes
        return generarPdfGenerico(obtenerEstadisticasCuidadores(), true);
    }

    @Override
    public byte[] generarReporteActividadesPdf() throws IOException {
        // Gráfico Actividades
        return generarPdfGenerico(obtenerEstadisticasActividadesCompletadas(), false);
    }

    private byte[] generarPdfGenerico(List<?> datos, boolean esReportePacientes) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();

            // titulos
            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
            String titulo = esReportePacientes ? "Reporte de Carga Laboral (Pacientes)" : "Reporte de Eficiencia (Actividades)";
            Paragraph pTitulo = new Paragraph(titulo, fontTitulo);
            pTitulo.setAlignment(Element.ALIGN_CENTER);
            document.add(pTitulo);

            // txt explicativo reporte actividades
            if (!esReportePacientes) {
                Font fontDesc = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
                Paragraph pDesc = new Paragraph("A continuación se muestra el desempeño de los cuidadores en base al ratio actividades completadas / actividades asignadas.", fontDesc);
                pDesc.setAlignment(Element.ALIGN_CENTER);
                pDesc.setSpacingBefore(10);
                pDesc.setSpacingAfter(10);
                document.add(pDesc);
            }
            document.add(Chunk.NEWLINE);

            // grafico
            JFreeChart chart = esReportePacientes
                    ? crearGraficoPacientes((List<EstadisticaCuidadorDTO>) datos)
                    : crearGraficoActividades((List<EstadisticaActividadDTO>) datos);

            BufferedImage chartImage = chart.createBufferedImage(500, 300);
            Image pdfImage = Image.getInstance(chartImage, null);
            pdfImage.setAlignment(Element.ALIGN_CENTER);
            document.add(pdfImage);
            document.add(Chunk.NEWLINE);

            // tbl
            PdfPTable table = new PdfPTable(esReportePacientes ? 2 : 4);
            table.setWidthPercentage(100);

            if (esReportePacientes) {
                agregarCeldaHeader(table, "Cuidador");
                agregarCeldaHeader(table, "Pacientes Asignados");

                for (EstadisticaCuidadorDTO d : (List<EstadisticaCuidadorDTO>) datos) {
                    table.addCell(new Phrase(d.getNombreCuidador() + " " + d.getApellidoCuidador()));
                    agregarCeldaCentro(table, d.getCantidadPacientes().toString());
                }
            } else {
                agregarCeldaHeader(table, "Cuidador");
                agregarCeldaHeader(table, "Actividades (Total)");
                agregarCeldaHeader(table, "Completadas");
                agregarCeldaHeader(table, "Efectividad");

                for (EstadisticaActividadDTO d : (List<EstadisticaActividadDTO>) datos) {
                    table.addCell(new Phrase(d.getNombreCuidador() + " " + d.getApellidoCuidador()));
                    agregarCeldaCentro(table, d.getActividadesAsignadas().toString());
                    agregarCeldaCentro(table, d.getActividadesCompletadas().toString());

                    double ratio = d.getActividadesAsignadas() > 0 ? (double) d.getActividadesCompletadas() / d.getActividadesAsignadas() * 100 : 0;
                    agregarCeldaCentro(table, String.format("%.0f%%", ratio));
                }
            }
            document.add(table);
            document.close();
            return out.toByteArray();
        }
    }

    // Helpers JFreeChart
    private JFreeChart crearGraficoPacientes(List<EstadisticaCuidadorDTO> datos) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        datos.forEach(d -> dataset.addValue(d.getCantidadPacientes(), "Pacientes", d.getNombreCuidador() + " " + d.getApellidoCuidador()));
        return ChartFactory.createBarChart("Pacientes por Cuidador", "Cuidador", "Cantidad", dataset, PlotOrientation.VERTICAL, false, true, false);
    }

    private JFreeChart crearGraficoActividades(List<EstadisticaActividadDTO> datos) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (EstadisticaActividadDTO d : datos) {
            String nombre = d.getNombreCuidador() + " " + d.getApellidoCuidador();
            dataset.addValue(d.getActividadesAsignadas(), "Total", nombre);
            dataset.addValue(d.getActividadesCompletadas(), "Realizadas", nombre);
        }
        JFreeChart chart = ChartFactory.createBarChart("Eficiencia", "Cuidador", "Actividades", dataset, PlotOrientation.VERTICAL, true, true, false);

        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setItemMargin(0.0); // barras pegadas
        renderer.setSeriesPaint(0, new Color(13, 110, 253)); // Azul
        renderer.setSeriesPaint(1, new Color(25, 135, 84));  // Verde

        plot.setBackgroundPaint(Color.white);
        plot.setRangeGridlinePaint(Color.gray);
        return chart;
    }

    private void agregarCeldaHeader(PdfPTable table, String text) {
        PdfPCell c = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE)));
        c.setBackgroundColor(Color.DARK_GRAY);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setPadding(5);
        table.addCell(c);
    }

    private void agregarCeldaCentro(PdfPTable table, String text) {
        PdfPCell c = new PdfPCell(new Phrase(text));
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c);
    }
}