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
import java.time.LocalDate;

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
        return pacienteAsignadoRepository.obtenerPacientesPorCuidador();
    }

    // grafica diaria
    @Override
    public List<EstadisticaActividadDTO> obtenerEstadisticasActividadesCompletadas() {
        // prepara la lista de datos que se enviara al navegador para la grafica web
        List<EstadisticaActividadDTO> dtos = new ArrayList<>();
        List<Usuario> cuidadores = usuarioRepository.findByRol_RolNombre(RolNombre.Cuidador);

        // fija la fecha actual para filtrar solo las act de hoy
        LocalDate hoy = LocalDate.now();

        for (Usuario u : cuidadores) {
            if (u instanceof Cuidador) {
                Cuidador cuidador = (Cuidador) u;

                // consulta la bd filtrando por fecha y excluyendo eliminados
                Long totalHoy = actividadRepository.countActividadesAsignadasPorFecha(cuidador, hoy); // solo hoy
                Long completadasHoy = actividadRepository.countActividadesCompletadasPorFecha(cuidador, EstadoActividad.Completada, hoy); // solo hoy

                // agrega el resultado a la lista (sin generar imagen, el navegador dibuja la grafica)
                dtos.add(new EstadisticaActividadDTO(
                        cuidador.getNombre(),
                        cuidador.getApellido(),
                        totalHoy,
                        completadasHoy
                ));
            }
        }
        return dtos;
    }

    private List<EstadisticaActividadDTO> obtenerDatosHistoricos() {
        // prepara los datos acumulados para el reporte en pdf
        List<EstadisticaActividadDTO> dtos = new ArrayList<>();
        List<Usuario> cuidadores = usuarioRepository.findByRol_RolNombre(RolNombre.Cuidador);

        for (Usuario u : cuidadores) {
            if (u instanceof Cuidador) {
                Cuidador cuidador = (Cuidador) u;

                // consulta el total de act (sin filtrar por fecha)
                Long total = actividadRepository.countTotalActividadesAsignadas(cuidador); // todo
                Long completadas = actividadRepository.countActividadesByCuidadorAndEstado(cuidador, EstadoActividad.Completada); // todo

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

    // PDFs

    // llama internamente a generarPdfGenerico (true). El true indica usar datos pacientes y no actividades
    @Override
    public byte[] generarReportePdf() throws IOException {
        return generarPdfGenerico(obtenerEstadisticasCuidadores(), true);
    }

    @Override
    public byte[] generarReporteActividadesPdf() throws IOException {
        // obtiene los datos totales para pasarlos al generador del pdf
        List<EstadisticaActividadDTO> datosHistoricos = obtenerDatosHistoricos();
        return generarPdfGenerico(datosHistoricos, false);
    }

    private byte[] generarPdfGenerico(List<?> datos, boolean esReportePacientes) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // crea el documento pdf en memoria con tamaño a4
            Document document = new Document(PageSize.A4);
            // asocia el escritor del pdf al flujo de salida de bytes
            PdfWriter.getInstance(document, out);
            // abre el documento para empezar a escribir contenido
            document.open();

            // define la fuente y el color para el titulo principal
            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
            String titulo = esReportePacientes ? "Reporte de Carga Laboral (Pacientes)" : "Reporte Total de Eficiencia";

            // crea el parrafo del titulo y lo alinea al centro
            Paragraph pTitulo = new Paragraph(titulo, fontTitulo);
            pTitulo.setAlignment(Element.ALIGN_CENTER);
            document.add(pTitulo);

            // agrega descripcion extra si es el reporte de actividades
            if (!esReportePacientes) {
                Font fontDesc = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
                Paragraph pDesc = new Paragraph("Desempeño acumulado total de los cuidadores.", fontDesc);
                pDesc.setAlignment(Element.ALIGN_CENTER);
                pDesc.setSpacingBefore(10);
                pDesc.setSpacingAfter(10);
                document.add(pDesc);
            }
            // salto linea vacio
            document.add(Chunk.NEWLINE);

            // genera grafica usando libreria de java jfreechart
            JFreeChart chart = esReportePacientes
                    ? crearGraficoPacientes((List<EstadisticaCuidadorDTO>) datos)
                    : crearGraficoActividades((List<EstadisticaActividadDTO>) datos);

            // convierte la grafica generada en java a una imagen png en memoria
            BufferedImage chartImage = chart.createBufferedImage(500, 300);
            // crea el objeto imagen de pdf a partir de la imagen en memoria
            Image pdfImage = Image.getInstance(chartImage, null);
            pdfImage.setAlignment(Element.ALIGN_CENTER);
            document.add(pdfImage);
            document.add(Chunk.NEWLINE);

            // crea la tabla pdf definiendo el numero de columnas segun el reporte
            PdfPTable table = new PdfPTable(esReportePacientes ? 2 : 4);
            table.setWidthPercentage(100);

            if (esReportePacientes) {
                // agrega los encabezados de la tabla
                agregarCeldaHeader(table, "Cuidador");
                agregarCeldaHeader(table, "Pacientes Asignados");
                // recorre los datos y llena las filas de la tabla
                for (EstadisticaCuidadorDTO d : (List<EstadisticaCuidadorDTO>) datos) {
                    table.addCell(new Phrase(d.getNombreCuidador() + " " + d.getApellidoCuidador()));
                    agregarCeldaCentro(table, d.getCantidadPacientes().toString());
                }
            } else {
                // agrega encabezados para el reporte de actividades
                agregarCeldaHeader(table, "Cuidador");
                agregarCeldaHeader(table, "Actividades (Total)");
                agregarCeldaHeader(table, "Completadas");
                agregarCeldaHeader(table, "Efectividad");

                for (EstadisticaActividadDTO d : (List<EstadisticaActividadDTO>) datos) {
                    table.addCell(new Phrase(d.getNombreCuidador() + " " + d.getApellidoCuidador()));
                    agregarCeldaCentro(table, d.getActividadesAsignadas().toString());
                    agregarCeldaCentro(table, d.getActividadesCompletadas().toString());

                    // calcula el porcentaje de efectividad para mostrar en la tabla
                    double ratio = d.getActividadesAsignadas() > 0 ? (double) d.getActividadesCompletadas() / d.getActividadesAsignadas() * 100 : 0;
                    agregarCeldaCentro(table, String.format("%.0f%%", ratio));
                }
            }
            // agrega la tabla finalizada al documento
            document.add(table);
            // cierra el documento para finalizar la escritura
            document.close();
            return out.toByteArray();
        }
    }

    // Helpers JFreeChart
    private JFreeChart crearGraficoPacientes(List<EstadisticaCuidadorDTO> datos) {
        // inicializa el conjunto de datos vacio para categorias
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        // llena el dataset con los nombres y cantidades de pacientes
        datos.forEach(d -> dataset.addValue(d.getCantidadPacientes(), "Pacientes", d.getNombreCuidador() + " " + d.getApellidoCuidador()));
        // crea y devuelve el grafico de barras 3d o estandar usando la fabrica de charts
        return ChartFactory.createBarChart("Pacientes por Cuidador", "Cuidador", "Cantidad", dataset, PlotOrientation.VERTICAL, false, true, false);
    }

    private JFreeChart crearGraficoActividades(List<EstadisticaActividadDTO> datos) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (EstadisticaActividadDTO d : datos) {
            String nombre = d.getNombreCuidador() + " " + d.getApellidoCuidador();
            // agrega la serie de actividades asignadas
            dataset.addValue(d.getActividadesAsignadas(), "Asignadas", nombre);
            // agrega la serie de actividades completadas
            dataset.addValue(d.getActividadesCompletadas(), "Realizadas", nombre);
        }
        // genera el grafico de barras agrupadas vertical
        JFreeChart chart = ChartFactory.createBarChart("Eficiencia de Actividades", "Cuidador", "Cantidad", dataset, PlotOrientation.VERTICAL, true, true, false);

        // obtiene el area de dibujo (para personalizar colores)
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        // elimina espacio entre barras de la misma categoria
        renderer.setItemMargin(0.0);
        // asigna color azul a la primera y verde segunda
        renderer.setSeriesPaint(0, new Color(13, 110, 253));
        renderer.setSeriesPaint(1, new Color(25, 135, 84));

        // fondo blanco y lineas grises
        plot.setBackgroundPaint(Color.white);
        plot.setRangeGridlinePaint(Color.gray);
        return chart;
    }

    private void agregarCeldaHeader(PdfPTable table, String text) {
        // crea celda de pdf
        PdfPCell c = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE)));
        // color de fondo gris oscuro para encabezado
        c.setBackgroundColor(Color.DARK_GRAY);
        // alinea el texto al centro horizontalmente
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        // agrega espacio interno a la celda (pa que no se vea apretado)
        c.setPadding(5);
        // añade la celda configurada a la tabla
        table.addCell(c);
    }

    private void agregarCeldaCentro(PdfPTable table, String text) {
        // crea una celda estandar con el texto proporcionado
        PdfPCell c = new PdfPCell(new Phrase(text));
        // fuerza la alineacion del contenido al centro
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        // añade la celda a la tabla
        table.addCell(c);
    }
}