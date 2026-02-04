package com.example.Gericare.Impl;

import com.example.Gericare.DTO.*;
import com.example.Gericare.Repository.*;
import com.example.Gericare.Service.EmailService;
import com.example.Gericare.Service.UsuarioService;
import com.example.Gericare.Service.PacienteAsignadoService;
import com.example.Gericare.Entity.*;
import com.example.Gericare.Enums.EstadoAsignacion;
import com.example.Gericare.Enums.EstadoUsuario;
import com.example.Gericare.Enums.RolNombre;
import com.example.Gericare.specification.UsuarioSpecification;

// Imports para PDF (OpenPDF)
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

// Imports para Excel (Apache POI)
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RolRepository rolRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private PacienteAsignadoRepository pacienteAsignadoRepository;
    @Autowired @Lazy private PacienteAsignadoService pacienteAsignadoService;
    @Autowired private EmailService emailService;

    // --- MÉTODOS DE RECUPERACIÓN DE CONTRASEÑA ---

    @Override
    public void createPasswordResetTokenForUser(String email) {
        Usuario usuario = usuarioRepository.findByCorreoElectronico(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        String token = UUID.randomUUID().toString();
        usuario.setResetPasswordToken(token);
        usuario.setResetPasswordTokenExpiryDate(LocalDateTime.now().plusHours(1));
        
        usuarioRepository.save(usuario);

        // =========================================================================
        // AQUÍ ESTÁ EL CAMBIO. URL DE AZURE "QUEMADA" PARA QUE NO FALLE JAMÁS
        // =========================================================================
        String dominio = "https://gericare-web-2026-beh2e0ajecf3h4a4.westus3-01.azurewebsites.net";
        
        String resetUrl = dominio + "/reset-password?token=" + token;
        
        // Enviamos el correo con el link EXACTO
        emailService.sendPasswordResetEmail(usuario.getCorreoElectronico(), resetUrl);
    }

    @Override
    public String validatePasswordResetToken(String token) {
        final Usuario usuario = usuarioRepository.findByResetPasswordToken(token).orElse(null);
        if (usuario == null || usuario.getResetPasswordTokenExpiryDate().isBefore(LocalDateTime.now())) {
            return "invalidToken";
        }
        return null;
    }

    @Override
    @Transactional
    public void changeUserPassword(String token, String newPassword) {
        Usuario usuario = usuarioRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        usuario.setContrasena(passwordEncoder.encode(newPassword));
        usuario.setResetPasswordToken(null);
        usuario.setResetPasswordTokenExpiryDate(null);
        
        // IMPORTANTE: Esto permite que entren sin pedir cambio de clave de nuevo
        usuario.setNecesitaCambioContrasena(false); 
        
        usuarioRepository.save(usuario);
    }

    // --- RESTO DE MÉTODOS (SIN CAMBIOS, SOLO LOS COPIO PARA QUE TENGAS EL ARCHIVO ENTERO) ---

    @Override
    public void sendCustomBulkEmailToRole(RolNombre role, String subject, String body) {
        List<Usuario> targetUsers = new ArrayList<>();
        if (role == null) {
            targetUsers.addAll(usuarioRepository.findByRol_RolNombre(RolNombre.Familiar));
            targetUsers.addAll(usuarioRepository.findByRol_RolNombre(RolNombre.Cuidador));
        } else {
            targetUsers = usuarioRepository.findByRol_RolNombre(role);
        }
        List<String> recipientEmails = targetUsers.stream()
                .map(Usuario::getCorreoElectronico)
                .collect(Collectors.toList());
        if (!recipientEmails.isEmpty()) {
            emailService.sendBulkEmail(recipientEmails, subject, body);
        }
    }

    @Override
    @Transactional
    public UsuarioDTO crearCuidador(Cuidador cuidador) {
        Optional<Usuario> usuarioExistente = usuarioRepository.findByDocumentoOrEmailNative(
                cuidador.getDocumentoIdentificacion(), cuidador.getCorreoElectronico());

        if (usuarioExistente.isPresent()) {
            Usuario existente = usuarioExistente.get();
            if (existente.getEstado() == EstadoUsuario.Activo) {
                throw new DataIntegrityViolationException("El usuario ya existe.");
            }
            if (existente instanceof Cuidador) {
                Cuidador recuperado = (Cuidador) existente;
                recuperado.setNombre(cuidador.getNombre());
                recuperado.setApellido(cuidador.getApellido());
                recuperado.setDireccion(cuidador.getDireccion());
                recuperado.setCorreoElectronico(cuidador.getCorreoElectronico());
                recuperado.setContrasena(passwordEncoder.encode(cuidador.getContrasena()));
                recuperado.setFechaContratacion(cuidador.getFechaContratacion());
                recuperado.setTipoContrato(cuidador.getTipoContrato());
                recuperado.setContactoEmergencia(cuidador.getContactoEmergencia());
                recuperado.setFechaNacimiento(cuidador.getFechaNacimiento());
                actualizarTelefonos(recuperado, cuidador.getTelefonos());
                recuperado.setEstado(EstadoUsuario.Activo);
                recuperado.setNecesitaCambioContrasena(true);
                return toDTO(usuarioRepository.save(recuperado));
            } else {
                throw new DataIntegrityViolationException("Documento registrado con otro rol.");
            }
        }
        cuidador.setContrasena(passwordEncoder.encode(cuidador.getContrasena()));
        Rol rol = rolRepository.findByRolNombre(RolNombre.Cuidador).orElseThrow();
        cuidador.setRol(rol);
        if (cuidador.getTelefonos() != null) cuidador.getTelefonos().forEach(t -> t.setUsuario(cuidador));
        cuidador.setNecesitaCambioContrasena(true);
        cuidador.setEstado(EstadoUsuario.Activo);
        return toDTO(usuarioRepository.save(cuidador));
    }

    @Override
    @Transactional
    public UsuarioDTO crearFamiliar(Familiar familiar) {
        if (familiar.getTelefonos() != null && familiar.getTelefonos().size() > 3) {
            throw new IllegalStateException("Máximo 3 teléfonos.");
        }
        Optional<Usuario> usuarioExistente = usuarioRepository.findByDocumentoOrEmailNative(
                familiar.getDocumentoIdentificacion(), familiar.getCorreoElectronico());

        if (usuarioExistente.isPresent()) {
            Usuario existente = usuarioExistente.get();
            if (existente.getEstado() == EstadoUsuario.Activo) {
                throw new DataIntegrityViolationException("El usuario ya existe.");
            }
            if (existente instanceof Familiar) {
                Familiar recuperado = (Familiar) existente;
                recuperado.setNombre(familiar.getNombre());
                recuperado.setApellido(familiar.getApellido());
                recuperado.setDireccion(familiar.getDireccion());
                recuperado.setCorreoElectronico(familiar.getCorreoElectronico());
                recuperado.setContrasena(passwordEncoder.encode(familiar.getContrasena()));
                recuperado.setParentesco(familiar.getParentesco());
                actualizarTelefonos(recuperado, familiar.getTelefonos());
                recuperado.setEstado(EstadoUsuario.Activo);
                recuperado.setNecesitaCambioContrasena(true);
                return toDTO(usuarioRepository.save(recuperado));
            } else {
                throw new DataIntegrityViolationException("Documento registrado con otro rol.");
            }
        }
        familiar.setContrasena(passwordEncoder.encode(familiar.getContrasena()));
        Rol rol = rolRepository.findByRolNombre(RolNombre.Familiar).orElseThrow();
        familiar.setRol(rol);
        if (familiar.getTelefonos() != null) familiar.getTelefonos().forEach(t -> t.setUsuario(familiar));
        familiar.setNecesitaCambioContrasena(true);
        familiar.setEstado(EstadoUsuario.Activo);
        return toDTO(usuarioRepository.save(familiar));
    }

    private void actualizarTelefonos(Usuario usuario, List<Telefono> nuevos) {
        if (usuario.getTelefonos() == null) usuario.setTelefonos(new ArrayList<>());
        else usuario.getTelefonos().clear();
        if (nuevos != null) {
            nuevos.forEach(t -> {
                t.setUsuario(usuario);
                usuario.getTelefonos().add(t);
            });
        }
    }

    @Override
    public List<UsuarioDTO> listarTodosLosUsuarios() {
        return usuarioRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public Optional<UsuarioDTO> obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id).map(this::toDTO);
    }

    @Override
    @Transactional
    public void eliminarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow();
        if (usuario instanceof Cuidador) {
             if (!pacienteAsignadoRepository.findByCuidador_idUsuarioAndEstado(id, EstadoAsignacion.Activo).isEmpty())
                 throw new IllegalStateException("Tiene pacientes asignados.");
        }
        if (usuario instanceof Familiar) {
            List<PacienteAsignado> asig = pacienteAsignadoRepository.findByFamiliar_idUsuario(id);
            if (!asig.isEmpty()) {
                asig.forEach(a -> a.setFamiliar(null));
                pacienteAsignadoRepository.saveAll(asig);
            }
        }
        usuario.setEstado(EstadoUsuario.Inactivo);
        usuarioRepository.save(usuario);
    }

    @Override
    public Optional<UsuarioDTO> findByEmail(String email) {
        return usuarioRepository.findByCorreoElectronico(email).map(this::toDTO);
    }

    @Override
    public Optional<UsuarioDTO> actualizarUsuario(Long id, UsuarioDTO dto) {
        return usuarioRepository.findById(id).map(u -> {
            if (dto.getCorreoElectronico() != null && !dto.getCorreoElectronico().equalsIgnoreCase(u.getCorreoElectronico())) {
                if (usuarioRepository.findByCorreoElectronico(dto.getCorreoElectronico()).isPresent())
                    throw new IllegalStateException("Correo en uso.");
                u.setCorreoElectronico(dto.getCorreoElectronico());
                emailService.sendEmailChangeNotification(u.getCorreoElectronico(), u.getNombre());
            }
            u.setNombre(dto.getNombre());
            u.setApellido(dto.getApellido());
            u.setDireccion(dto.getDireccion());
            if (u instanceof Empleado) {
                ((Empleado) u).setTipoContrato(dto.getTipoContrato());
                ((Empleado) u).setContactoEmergencia(dto.getContactoEmergencia());
            }
            if (dto.getTelefonos() != null) {
                u.getTelefonos().clear();
                dto.getTelefonos().stream().filter(n -> n != null && !n.isEmpty()).forEach(n -> {
                    Telefono t = new Telefono();
                    t.setNumero(n);
                    t.setUsuario(u);
                    u.getTelefonos().add(t);
                });
            }
            return toDTO(usuarioRepository.save(u));
        });
    }

    @Override
    public List<UsuarioDTO> findUsuariosByCriteria(String nombre, String documento, RolNombre rol) {
        return usuarioRepository.findAll(UsuarioSpecification.findByCriteria(nombre, documento, rol))
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<PacienteAsignadoDTO> findPacientesByCuidadorEmail(String email) {
        return pacienteAsignadoRepository.findByCuidador_CorreoElectronicoAndEstado(email, EstadoAsignacion.Activo)
                .stream().map(pacienteAsignadoService::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<PacienteAsignadoDTO> findPacientesByFamiliarEmail(String email) {
        return pacienteAsignadoRepository.findByFamiliar_CorreoElectronicoAndEstado(email, EstadoAsignacion.Activo)
                .stream().map(pacienteAsignadoService::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<UsuarioDTO> findByRol(RolNombre rolNombre) {
        return usuarioRepository.findByRol_RolNombre(rolNombre)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    private UsuarioDTO toDTO(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        setCommonProperties(usuario, dto);
        if (usuario instanceof Empleado) {
            Empleado e = (Empleado) usuario;
            dto.setFechaContratacion(e.getFechaContratacion());
            dto.setTipoContrato(e.getTipoContrato());
            dto.setContactoEmergencia(e.getContactoEmergencia());
            dto.setFechaNacimiento(e.getFechaNacimiento());
        } else if (usuario instanceof Familiar) {
            dto.setParentesco(((Familiar) usuario).getParentesco());
        }
        if (usuario.getTelefonos() != null) {
            dto.setTelefonos(usuario.getTelefonos().stream().map(Telefono::getNumero).collect(Collectors.toList()));
        }
        dto.setNecesitaCambioContrasena(usuario.isNecesitaCambioContrasena());
        return dto;
    }

    private void setCommonProperties(Usuario usuario, UsuarioDTO dto) {
        dto.setIdUsuario(usuario.getIdUsuario());
        dto.setTipoDocumento(usuario.getTipoDocumento());
        dto.setDocumentoIdentificacion(usuario.getDocumentoIdentificacion());
        dto.setNombre(usuario.getNombre());
        dto.setApellido(usuario.getApellido());
        dto.setDireccion(usuario.getDireccion());
        dto.setCorreoElectronico(usuario.getCorreoElectronico());
        dto.setEstado(usuario.getEstado());
        dto.setRol(usuario.getRol());
    }

    public List<Usuario> listarUsuarios() { return usuarioRepository.findAll(); }

    public void exportarUsuariosAExcel(OutputStream outputStream, String nombre, String documento, RolNombre rol) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Usuarios");
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Tipo Doc", "Documento", "Nombre", "Apellido", "Dirección", "Correo", "Rol"};
            for(int i=0; i<columns.length; i++) headerRow.createCell(i).setCellValue(columns[i]);

            List<Usuario> usuarios = usuarioRepository.findAll(UsuarioSpecification.findByCriteria(nombre, documento, rol));
            int rowNum = 1;
            for (Usuario u : usuarios) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(u.getIdUsuario());
                row.createCell(1).setCellValue(u.getTipoDocumento() != null ? u.getTipoDocumento().toString() : "");
                row.createCell(2).setCellValue(u.getDocumentoIdentificacion());
                row.createCell(3).setCellValue(u.getNombre());
                row.createCell(4).setCellValue(u.getApellido());
                row.createCell(5).setCellValue(u.getDireccion());
                row.createCell(6).setCellValue(u.getCorreoElectronico());
                row.createCell(7).setCellValue(u.getRol() != null ? u.getRol().getRolNombre().toString() : "");
            }
            for (int i = 0; i < columns.length; i++) sheet.autoSizeColumn(i);
            workbook.write(outputStream);
        }
    }

    public void exportarUsuariosAPDF(OutputStream outputStream, String nombre, String documento, RolNombre rol) throws IOException {
        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);
        document.open();
        document.add(new Paragraph("Lista de Usuarios - Gericare Connect"));
        document.add(new Paragraph(" "));
        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        String[] headers = {"ID", "Tipo Doc", "Documento", "Nombre", "Apellido", "Dirección", "Correo", "Rol"};
        for(String header : headers) table.addCell(header);
        List<Usuario> usuarios = usuarioRepository.findAll(UsuarioSpecification.findByCriteria(nombre, documento, rol));
        for (Usuario u : usuarios) {
            table.addCell(String.valueOf(u.getIdUsuario()));
            table.addCell(u.getTipoDocumento() != null ? u.getTipoDocumento().toString() : "");
            table.addCell(u.getDocumentoIdentificacion());
            table.addCell(u.getNombre());
            table.addCell(u.getApellido());
            table.addCell(u.getDireccion());
            table.addCell(u.getCorreoElectronico());
            table.addCell(u.getRol() != null ? u.getRol().getRolNombre().toString() : "");
        }
        document.add(table);
        document.close();
    }
}
