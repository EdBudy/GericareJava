package com.example.Gericare.Impl;

import com.example.Gericare.DTO.*;
import com.example.Gericare.Repository.PacienteAsignadoRepository;
import com.example.Gericare.Repository.RolRepository;
import com.example.Gericare.Service.UsuarioService;
import com.example.Gericare.Repository.UsuarioRepository;
import com.example.Gericare.Service.PacienteAsignadoService;
import com.example.Gericare.entity.*;
import com.example.Gericare.enums.EstadoAsignacion;
import com.example.Gericare.enums.EstadoUsuario;
import com.example.Gericare.enums.RolNombre;
import com.example.Gericare.specification.UsuarioSpecification;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.lowagie.text.pdf.PdfWriter;



import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    // --- DEPENDENCIAS INYECTADAS ---
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private RolRepository rolRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    // --- NUEVAS DEPENDENCIAS AÑADIDAS ---
    @Autowired
    private PacienteAsignadoRepository pacienteAsignadoRepository;
    @Autowired
    @Lazy // Se usa @Lazy para romper una posible dependencia circular al inyectar servicios entre sí
    private PacienteAsignadoService pacienteAsignadoService;


    // --- MÉTODOS DE CREACIÓN (Sin cambios) ---
    @Override
    public UsuarioDTO crearCuidador(Cuidador cuidador) {
        cuidador.setContrasena(passwordEncoder.encode(cuidador.getContrasena()));
        Rol rolCuidador = rolRepository.findByRolNombre(RolNombre.Cuidador)
                .orElseThrow(() -> new RuntimeException("Error: Rol 'Cuidador' no encontrado."));
        cuidador.setRol(rolCuidador);
        Cuidador cuidadorGuardado = usuarioRepository.save(cuidador);
        return toDTO(cuidadorGuardado);
    }

    @Override
    public UsuarioDTO crearFamiliar(Familiar familiar) {
        if (familiar.getTelefonos() != null && familiar.getTelefonos().size() > 3) {
            throw new IllegalStateException("Un familiar no puede tener más de 3 teléfonos.");
        }
        familiar.setContrasena(passwordEncoder.encode(familiar.getContrasena()));
        Rol rolFamiliar = rolRepository.findByRolNombre(RolNombre.Familiar)
                .orElseThrow(() -> new RuntimeException("Error: Rol 'Familiar' no encontrado."));
        familiar.setRol(rolFamiliar);
        if (familiar.getTelefonos() != null) {
            familiar.getTelefonos().forEach(telefono -> telefono.setUsuario(familiar));
        }
        Familiar familiarGuardado = usuarioRepository.save(familiar);
        return toDTO(familiarGuardado);
    }

    // --- MÉTODOS DE CONSULTA (Sin cambios) ---
    @Override
    public List<UsuarioDTO> listarTodosLosUsuarios() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<UsuarioDTO> obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id).map(this::toDTO);
    }

    // --- MÉTODOS DE GESTIÓN (Sin cambios) ---
    @Override
    public void eliminarUsuario(Long id) {
        usuarioRepository.findById(id).ifPresent(usuario -> {
            usuario.setEstado(EstadoUsuario.Inactivo);
            usuarioRepository.save(usuario);
        });
    }

    @Override
    public Optional<UsuarioDTO> actualizarUsuario(Long id, UsuarioDTO usuarioDTO) {
        return usuarioRepository.findById(id).map(usuarioExistente -> {
            usuarioExistente.setNombre(usuarioDTO.getNombre());
            usuarioExistente.setApellido(usuarioDTO.getApellido());
            usuarioExistente.setDireccion(usuarioDTO.getDireccion());
            Usuario usuarioActualizado = usuarioRepository.save(usuarioExistente);
            return toDTO(usuarioActualizado);
        });
    }


    // --- NUEVOS MÉTODOS PARA EL DASHBOARD Y FILTROS ---

    @Override
    public List<UsuarioDTO> findUsuariosByCriteria(String nombre, String documento, RolNombre rol) {
        // Usa la clase Specification para construir una consulta con los filtros proporcionados.
        return usuarioRepository.findAll(UsuarioSpecification.findByCriteria(nombre, documento, rol))
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PacienteAsignadoDTO> findPacientesByCuidadorEmail(String email) {
        // Busca en el repositorio de asignaciones por el email del cuidador y el estado activo.
        return pacienteAsignadoRepository.findByCuidador_CorreoElectronicoAndEstado(email, EstadoAsignacion.Activo)
                .stream()
                // Reutiliza la lógica de conversión de PacienteAsignadoServiceImpl
                .map(pacienteAsignadoService::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PacienteAsignadoDTO> findPacientesByFamiliarEmail(String email) {
        // Busca en el repositorio de asignaciones por el email del familiar y el estado activo.
        return pacienteAsignadoRepository.findByFamiliar_CorreoElectronicoAndEstado(email, EstadoAsignacion.Activo)
                // Reutiliza la lógica de conversión
                .map(pacienteAsignadoService::toDTO);
    }


    // --- MÉTODOS PRIVADOS DE CONVERSIÓN (Sin cambios) ---
    private UsuarioDTO toDTO(Usuario usuario) {
        if (usuario instanceof Empleado) {
            return toEmpleadoDTO((Empleado) usuario);
        }
        if (usuario instanceof Familiar) {
            return toFamiliarDTO((Familiar) usuario);
        }
        // Puedes añadir un mapeo base para otros tipos de Usuario si es necesario
        UsuarioDTO dto = new UsuarioDTO();
        setCommonProperties(usuario, dto);
        return dto;
    }

    // Método para asignar propiedades comunes
    private void setCommonProperties(Usuario usuario, UsuarioDTO dto) {
        dto.setIdUsuario(usuario.getIdUsuario());
        dto.setTipoDocumento(usuario.getTipoDocumento());
        dto.setDocumentoIdentificacion(usuario.getDocumentoIdentificacion());
        dto.setNombre(usuario.getNombre());
        dto.setApellido(usuario.getApellido());
        dto.setDireccion(usuario.getDireccion());
        dto.setCorreoElectronico(usuario.getCorreoElectronico());
        dto.setEstado(usuario.getEstado());
        dto.setRol(usuario.getRol()); // <-- ASIGNACIÓN CLAVE
    }

    private EmpleadoDTO toEmpleadoDTO(Empleado empleado) {
        EmpleadoDTO dto = new EmpleadoDTO();
        setCommonProperties(empleado, dto); // Usa el método común
        dto.setFechaContratacion(empleado.getFechaContratacion());
        dto.setTipoContrato(empleado.getTipoContrato());
        dto.setContactoEmergencia(empleado.getContactoEmergencia());
        dto.setFechaNacimiento(empleado.getFechaNacimiento());
        return dto;
    }

    private FamiliarDTO toFamiliarDTO(Familiar familiar) {
        FamiliarDTO dto = new FamiliarDTO();
        setCommonProperties(familiar, dto); // Usa el método común
        dto.setParentesco(familiar.getParentesco());
        return dto;
    }

    // ------
    //listar todos los usuarios
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    //exportar al excel
    public void exportarUsuariosAExcel(OutputStream outputStream) throws IOException {
        //Libro
        Workbook workbook = new XSSFWorkbook();
        //Hoja
        Sheet sheet = workbook.createSheet("Usuarios");
        //Fila de encabezado
        Row headerRow = sheet.createRow(0);
        //Columna de encabezado
        headerRow.createCell(0).setCellValue("ID");
        headerRow.createCell(1).setCellValue("Tipo Documento");
        headerRow.createCell(2).setCellValue("Documento Identificación");
        headerRow.createCell(3).setCellValue("Nombre");
        headerRow.createCell(4).setCellValue("Apellido");
        headerRow.createCell(5).setCellValue("Dirección");
        headerRow.createCell(6).setCellValue("Correo Electrónico");
        headerRow.createCell(7).setCellValue("Rol");

        //Listar todos
        List<Usuario> usuarios = listarUsuarios();

        //Llenar filas
        int rowNum = 1;
        for (Usuario usuario : usuarios) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(usuario.getIdUsuario());
            row.createCell(1).setCellValue(usuario.getTipoDocumento().toString());
            row.createCell(2).setCellValue(usuario.getDocumentoIdentificacion());
            row.createCell(3).setCellValue(usuario.getNombre());
            row.createCell(4).setCellValue(usuario.getApellido());
            row.createCell(5).setCellValue(usuario.getDireccion());
            row.createCell(6).setCellValue(usuario.getCorreoElectronico());
            row.createCell(7).setCellValue(usuario.getRol().getRolNombre().toString());
        }

        //Ajustar tamaño de las columnas
        for (int i = 0; i < 8; i++) {
            sheet.autoSizeColumn(i);
        }
        //Escribir en el OutputStream
        workbook.write(outputStream);
        workbook.close();

    }

    //Exportar a PDF
    public void exportarUsuariosAPDF(OutputStream outputStream) throws IOException {
        //Crear documento PDF
        Document document = new Document();

        //Acosiar al OutputStream
        PdfWriter.getInstance(document, outputStream);

        //Abrir documento
        document.open();

        //Agregar contenido
        document.add(new Paragraph("Lista de Usuarios"));
        document.add(new Paragraph(" ")); // Línea en blanco

        //Crear tabla con 8 columnas
        PdfPTable table = new PdfPTable(8);

        //Agregar encabezados
        table.addCell("ID");
        table.addCell("Tipo Documento");
        table.addCell("Documento Identificación");
        table.addCell("Nombre");
        table.addCell("Apellido");
        table.addCell("Dirección");
        table.addCell("Correo Electrónico");
        table.addCell("Rol");

        //Listar todos
        List<Usuario> usuarios = listarUsuarios();

        for (Usuario usuario : usuarios) {
            table.addCell(String.valueOf(usuario.getIdUsuario()));
            table.addCell(usuario.getTipoDocumento().toString());
            table.addCell(usuario.getDocumentoIdentificacion());
            table.addCell(usuario.getNombre());
            table.addCell(usuario.getApellido());
            table.addCell(usuario.getDireccion());
            table.addCell(usuario.getCorreoElectronico());
            table.addCell(usuario.getRol().getRolNombre().toString());
        }
    }
}