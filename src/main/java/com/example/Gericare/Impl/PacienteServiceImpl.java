package com.example.Gericare.Impl;

import com.example.Gericare.DTO.PacienteDTO;
import com.example.Gericare.Repository.ActividadRepository;
import com.example.Gericare.Repository.PacienteAsignadoRepository;
import com.example.Gericare.Repository.PacienteRepository;
import com.example.Gericare.Service.PacienteAsignadoService;
import com.example.Gericare.Service.PacienteService;
import com.example.Gericare.Entity.Actividad;
import com.example.Gericare.Entity.Paciente;
import com.example.Gericare.Entity.PacienteAsignado;
import com.example.Gericare.Enums.EstadoActividad;
import com.example.Gericare.Enums.EstadoAsignacion;
import com.example.Gericare.Enums.EstadoPaciente;
import com.example.Gericare.specification.PacienteSpecification;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.Gericare.Entity.Solicitud;
import com.example.Gericare.Entity.Tratamiento;
import com.example.Gericare.Enums.EstadoSolicitud;
import com.example.Gericare.Repository.SolicitudRepository;
import com.example.Gericare.Repository.TratamientoRepository;

import java.io.IOException;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PacienteServiceImpl implements PacienteService {

    @Autowired
    private PacienteRepository pacienteRepository;
    @Autowired
    private PacienteAsignadoService pacienteAsignadoService;
    @Autowired
    private PacienteAsignadoRepository pacienteAsignadoRepository;
    @Autowired
    private ActividadRepository actividadRepository;
    @Autowired
    private SolicitudRepository solicitudRepository;
    @Autowired
    private TratamientoRepository tratamientoRepository;

    @Override
    @Transactional
    public PacienteDTO crearPacienteYAsignar(PacienteDTO pacienteDTO, Long cuidadorId, Long familiarId, Long adminId) {
        Paciente nuevoPaciente = toEntity(pacienteDTO);
        nuevoPaciente.setEstado(EstadoPaciente.Activo);
        Paciente pacienteGuardado = pacienteRepository.save(nuevoPaciente);
        pacienteAsignadoService.crearAsignacion(pacienteGuardado.getIdPaciente(), cuidadorId, familiarId, adminId);
        return toDTO(pacienteGuardado);
    }

    @Override
    public List<PacienteDTO> listarPacientesFiltrados(String nombre, String documento) {
        List<Paciente> pacientes = pacienteRepository.findAll(PacienteSpecification.findByCriteria(nombre, documento));
        return pacientes.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public Optional<PacienteDTO> obtenerPacientePorId(Long id) {
        return pacienteRepository.findById(id).map(this::toDTO);
    }

    @Override
    @Transactional
    public void actualizarPacienteYReasignar(Long pacienteId, PacienteDTO pacienteDTO, Long cuidadorId, Long familiarId, Long adminId) {
        Paciente pacienteExistente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new RuntimeException("No se encontró el paciente con ID: " + pacienteId));

        pacienteExistente.setContactoEmergencia(pacienteDTO.getContactoEmergencia());
        pacienteExistente.setEstadoCivil(pacienteDTO.getEstadoCivil());
        pacienteExistente.setSeguroMedico(pacienteDTO.getSeguroMedico());
        pacienteExistente.setNumeroSeguro(pacienteDTO.getNumeroSeguro());

        pacienteAsignadoService.crearAsignacion(pacienteId, cuidadorId, familiarId, adminId);
    }

    @Override
    @Transactional
    public void eliminarPaciente(Long id) {
        pacienteRepository.findById(id).ifPresent(paciente -> {
            // Desactivar paciente
            paciente.setEstado(EstadoPaciente.Inactivo);
            pacienteRepository.save(paciente);

            // Buscar todas las asignaciones del paciente y desactivarlas
            List<PacienteAsignado> todasLasAsignaciones = pacienteAsignadoRepository.findByPacienteIdPaciente(id);
            todasLasAsignaciones.forEach(asignacion -> asignacion.setEstado(EstadoAsignacion.Inactivo));
            pacienteAsignadoRepository.saveAll(todasLasAsignaciones);

            // Desactivar actividades del paciente
            List<Actividad> actividades = actividadRepository.findByPacienteIdPaciente(id);
            actividades.forEach(actividad -> actividad.setEstadoActividad(EstadoActividad.Inactivo));
            actividadRepository.saveAll(actividades);

            // Desactivar Solicitudes paciente
            List<Solicitud> solicitudes = solicitudRepository.findByPacienteIdPaciente(id);
            solicitudes.forEach(solicitud -> {
                if (solicitud.getEstadoSolicitud() != EstadoSolicitud.Inactivo) {
                    solicitud.setEstadoSolicitud(EstadoSolicitud.Inactivo);
                }
            });
            solicitudRepository.saveAll(solicitudes);

            // Desactivar Tratamientos paciente
            List<Tratamiento> tratamientos = tratamientoRepository.findByPacienteIdPaciente(id);
            tratamientos.forEach(tratamiento -> {
                if (tratamiento.getEstadoTratamiento() != EstadoActividad.Inactivo) {
                    tratamiento.setEstadoTratamiento(EstadoActividad.Inactivo);
                }
            });
            tratamientoRepository.saveAll(tratamientos);
        });
    }

    private String getNombreFamiliarAsociado(Long pacienteId) {
        return pacienteAsignadoRepository.findByPacienteIdPacienteAndEstado(pacienteId, EstadoAsignacion.Activo)
                .stream()
                .findFirst()
                .map(asignacion -> {
                    if (asignacion.getFamiliar() != null) {
                        return asignacion.getFamiliar().getNombre() + " " + asignacion.getFamiliar().getApellido();
                    }
                    return "N/A";
                })
                .orElse("N/A");
    }

    @Override
    public void exportarPacientesAExcel(OutputStream outputStream, String nombre, String documento) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Pacientes");
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("ID");
        headerRow.createCell(1).setCellValue("Nombre Completo");
        headerRow.createCell(2).setCellValue("Documento");
        headerRow.createCell(3).setCellValue("Fecha de Nacimiento");
        headerRow.createCell(4).setCellValue("Género");
        headerRow.createCell(5).setCellValue("Familiar Asociado");

        List<Paciente> pacientes = pacienteRepository.findAll(PacienteSpecification.findByCriteria(nombre, documento));

        int rowNum = 1;
        for (Paciente paciente : pacientes) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(paciente.getIdPaciente());
            row.createCell(1).setCellValue(paciente.getNombre() + " " + paciente.getApellido());
            row.createCell(2).setCellValue(paciente.getDocumentoIdentificacion());
            row.createCell(3).setCellValue(paciente.getFechaNacimiento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            row.createCell(4).setCellValue(paciente.getGenero().toString());
            row.createCell(5).setCellValue(getNombreFamiliarAsociado(paciente.getIdPaciente()));
        }

        for (int i = 0; i < 6; i++) {
            sheet.autoSizeColumn(i);
        }
        workbook.write(outputStream);
        workbook.close();
    }

    @Override
    public void exportarPacientesAPDF(OutputStream outputStream, String nombre, String documento) throws IOException {
        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);
        document.open();
        document.add(new Paragraph("Lista de Pacientes"));
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(6);
        table.addCell("ID");
        table.addCell("Nombre Completo");
        table.addCell("Documento");
        table.addCell("Fecha de Nacimiento");
        table.addCell("Género");
        table.addCell("Familiar Asociado");

        List<Paciente> pacientes = pacienteRepository.findAll(PacienteSpecification.findByCriteria(nombre, documento));

        for (Paciente paciente : pacientes) {
            table.addCell(String.valueOf(paciente.getIdPaciente()));
            table.addCell(paciente.getNombre() + " " + paciente.getApellido());
            table.addCell(paciente.getDocumentoIdentificacion());
            table.addCell(paciente.getFechaNacimiento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            table.addCell(paciente.getGenero().toString());
            table.addCell(getNombreFamiliarAsociado(paciente.getIdPaciente()));
        }
        document.add(table);
        document.close();
    }

    private PacienteDTO toDTO(Paciente paciente) {
        return new PacienteDTO(
                paciente.getIdPaciente(),
                paciente.getDocumentoIdentificacion(),
                paciente.getNombre(),
                paciente.getApellido(),
                paciente.getFechaNacimiento(),
                paciente.getGenero(),
                paciente.getContactoEmergencia(),
                paciente.getEstadoCivil(),
                paciente.getTipoSangre(),
                paciente.getSeguroMedico(),
                paciente.getNumeroSeguro(),
                paciente.getEstado());
    }

    private Paciente toEntity(PacienteDTO dto) {
        Paciente paciente = new Paciente();
        paciente.setIdPaciente(dto.getIdPaciente());
        paciente.setDocumentoIdentificacion(dto.getDocumentoIdentificacion());
        paciente.setNombre(dto.getNombre());
        paciente.setApellido(dto.getApellido());
        paciente.setFechaNacimiento(dto.getFechaNacimiento());
        paciente.setGenero(dto.getGenero());
        paciente.setContactoEmergencia(dto.getContactoEmergencia());
        paciente.setEstadoCivil(dto.getEstadoCivil());
        paciente.setTipoSangre(dto.getTipoSangre());
        paciente.setSeguroMedico(dto.getSeguroMedico());
        paciente.setNumeroSeguro(dto.getNumeroSeguro());
        return paciente;
    }
}