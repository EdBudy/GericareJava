package com.example.Gericare.Impl;

import com.example.Gericare.DTO.PacienteDTO;
import com.example.Gericare.Repository.PacienteAsignadoRepository;
import com.example.Gericare.Repository.PacienteRepository;
import com.example.Gericare.Repository.UsuarioRepository;
import com.example.Gericare.Service.PacienteAsignadoService;
import com.example.Gericare.Service.PacienteService;
import com.example.Gericare.entity.Paciente;
import com.example.Gericare.entity.PacienteAsignado;
import com.example.Gericare.entity.Usuario;
import com.example.Gericare.enums.EstadoAsignacion;
import com.example.Gericare.enums.EstadoPaciente;
import com.example.Gericare.specification.PacienteSpecification; // Importante
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

import java.io.IOException;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PacienteServiceImpl implements PacienteService {

    @Autowired
    private PacienteRepository pacienteRepository;
    @Autowired
    private PacienteAsignadoService pacienteAsignadoService;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PacienteAsignadoRepository pacienteAsignadoRepository;

    // --- TUS MÉTODOS EXISTENTES (SIN CAMBIOS) ---

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
    public PacienteDTO crearPaciente(PacienteDTO pacienteDTO) {
        Paciente nuevoPaciente = toEntity(pacienteDTO);
        nuevoPaciente.setEstado(EstadoPaciente.Activo);
        Paciente pacienteGuardado = pacienteRepository.save(nuevoPaciente);
        return toDTO(pacienteGuardado);
    }

    @Override
    public List<PacienteDTO> listarTodosLosPacientes() {
        return pacienteRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
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
        PacienteAsignado asignacionActual = pacienteAsignadoRepository.findByPacienteIdPacienteAndEstado(pacienteId, EstadoAsignacion.Activo)
                .stream().findFirst().orElse(null);
        boolean haCambiadoLaAsignacion = false;
        if (asignacionActual == null) {
            haCambiadoLaAsignacion = true;
        } else {
            Long familiarActualId = (asignacionActual.getFamiliar() != null) ? asignacionActual.getFamiliar().getIdUsuario() : null;
            if (!asignacionActual.getCuidador().getIdUsuario().equals(cuidadorId) ||
                    !Objects.equals(familiarActualId, familiarId)) {
                haCambiadoLaAsignacion = true;
            }
        }
        if (haCambiadoLaAsignacion) {
            if (familiarId != null) {
                Usuario familiar = usuarioRepository.findById(familiarId)
                        .orElseThrow(() -> new RuntimeException("No se encontró el familiar con ID: " + familiarId));
                pacienteExistente.setUsuarioFamiliar(familiar);
            } else {
                pacienteExistente.setUsuarioFamiliar(null);
            }
            pacienteAsignadoService.crearAsignacion(pacienteId, cuidadorId, familiarId, adminId);
        }
    }

    @Override
    public void eliminarPaciente(Long id) {
        pacienteRepository.findById(id).ifPresent(paciente -> {
            paciente.setEstado(EstadoPaciente.Inactivo);
            pacienteRepository.save(paciente);
        });
    }

    // --- NUEVA LÓGICA DE FILTRADO Y EXPORTACIÓN ---

    @Override
    public List<PacienteDTO> listarPacientesFiltrados(String nombre, String documento) {
        List<Paciente> pacientes = pacienteRepository.findAll(PacienteSpecification.findByCriteria(nombre, documento));
        return pacientes.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
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
            String familiarAsociado = paciente.getUsuarioFamiliar() != null ? paciente.getUsuarioFamiliar().getNombre() + " " + paciente.getUsuarioFamiliar().getApellido() : "N/A";
            row.createCell(5).setCellValue(familiarAsociado);
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
            String familiarAsociado = paciente.getUsuarioFamiliar() != null ? paciente.getUsuarioFamiliar().getNombre() + " " + paciente.getUsuarioFamiliar().getApellido() : "N/A";
            table.addCell(familiarAsociado);
        }
        document.add(table);
        document.close();
    }


    // --- TUS MÉTODOS PRIVADOS (SIN CAMBIOS) ---

    private PacienteDTO toDTO(Paciente paciente) {
        String nombreFamiliar = null;
        if (paciente.getUsuarioFamiliar() != null) {
            nombreFamiliar = paciente.getUsuarioFamiliar().getNombre() + " "
                    + paciente.getUsuarioFamiliar().getApellido();
        }
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
                paciente.getEstado(),
                nombreFamiliar);
    }

    private Paciente toEntity(PacienteDTO dto) {
        Paciente paciente = new Paciente();
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