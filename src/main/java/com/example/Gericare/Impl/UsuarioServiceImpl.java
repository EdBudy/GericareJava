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

// Imports PDF y Excel
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // IMPORTANTE: Para leer la URL
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

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private RolRepository rolRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private PacienteAsignadoRepository pacienteAsignadoRepository;
    @Autowired
    @Lazy
    private PacienteAsignadoService pacienteAsignadoService;
    @Autowired
    private EmailService emailService;

    // 🟢 INYECCIÓN DE LA URL DESDE APPLICATION.PROPERTIES
    // Esto tomará el valor que definiste en el Paso 1
    @Value("${app.base-url}")
    private String baseUrl;

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
                cuidador.getDocumentoIdentificacion(),
                cuidador.getCorreoElectronico()
        );

        if (usuarioExistente.isPresent()) {
            Usuario existente = usuarioExistente.get();
            if (existente.getEstado() == EstadoUsuario.Activo) {
                throw new DataIntegrityViolationException("El usuario ya existe con ese documento o correo.");
            }
            if (existente instanceof Cuidador) {
                Cuidador cuidadorRecuperado = (Cuidador) existente;
                cuidadorRecuperado.setNombre(cuidador.getNombre());
                cuidadorRecuperado.setApellido(cuidador.getApellido());
                cuidadorRecuperado.setDireccion(cuidador.getDireccion());
                cuidadorRecuperado.setCorreoElectronico(cuidador.getCorreoElectronico());
                cuidadorRecuperado.setContrasena(passwordEncoder.encode(cuidador.getContrasena()));
                cuidadorRecuperado.setFechaContratacion(cuidador.getFechaContratacion());
                cuidadorRecuperado.setTipoContrato(cuidador.getTipoContrato());
                cuidadorRecuperado.setContactoEmergencia(cuidador.getContactoEmergencia());
                cuidadorRecuperado.setFechaNacimiento(cuidador.getFechaNacimiento());
                actualizarTelefonos(cuidadorRecuperado, cuidador.getTelefonos());
                cuidadorRecuperado.setEstado(EstadoUsuario.Activo);
                cuidadorRecuperado.setNecesitaCambioContrasena(true);
                return toDTO(usuarioRepository.save(cuidadorRecuperado));
            } else {
                throw new DataIntegrityViolationException("El documento ya existe registrado con otro rol y no se puede recuperar.");
            }
        }

        cuidador.setContrasena(passwordEncoder.encode(cuidador.getContrasena()));
        Rol rolCuidador = rolRepository.findByRolNombre(RolNombre.Cuidador)
                .orElseThrow(() -> new RuntimeException("Error: Rol 'Cuidador' no encontrado."));
        cuidador.setRol(rolCuidador);

        if (cuidador.getTelefonos() != null) {
            cuidador.getTelefonos().forEach(telefono -> telefono.setUsuario(cuidador));
        }
        cuidador.setNecesitaCambioContrasena(true);
        if (cuidador.getEstado() == null) {
            cuidador.setEstado(EstadoUsuario.Activo);
        }
        return toDTO(usuarioRepository.save(cuidador));
    }

    @Override
    @Transactional
    public UsuarioDTO crearFamiliar(Familiar familiar) {
        if (familiar.getTelefonos() != null && familiar.getTelefonos().size() > 3) {
            throw new IllegalStateException("Un familiar no puede tener más de 3 teléfonos.");
        }
        Optional<Usuario> usuarioExistente = usuarioRepository.findByDocumentoOrEmailNative(
                familiar.getDocumentoIdentificacion(),
                familiar.getCorreoElectronico()
        );

        if (usuarioExistente.isPresent()) {
            Usuario existente = usuarioExistente.get();
            if (existente.getEstado() == EstadoUsuario.Activo) {
                throw new DataIntegrityViolationException("El usuario ya existe con ese documento o correo.");
            }
            if (existente instanceof Familiar) {
                Familiar familiarRecuperado = (Familiar) existente;
                familiarRecuperado.setNombre(familiar.getNombre());
                familiarRecuperado.setApellido(familiar.getApellido());
                familiarRecuperado.setDireccion(familiar.getDireccion());
                familiarRecuperado.setCorreoElectronico(familiar.getCorreoElectronico());
                familiarRecuperado.setContrasena(passwordEncoder.encode(familiar.getContrasena()));
                familiarRecuperado.setParentesco(familiar.getParentesco());
                actualizarTelefonos(familiarRecuperado, familiar.getTelefonos());
                familiarRecuperado.setEstado(EstadoUsuario.Activo);
                familiarRecuperado.setNecesitaCambioContrasena(true);
                return toDTO(usuarioRepository.save(familiarRecuperado));
            } else {
                throw new DataIntegrityViolationException("El documento ya existe registrado con otro rol.");
            }
        }

        familiar.setContrasena(passwordEncoder.encode(familiar.getContrasena()));
        Rol rolFamiliar = rolRepository.findByRolNombre(RolNombre.Familiar)
                .orElseThrow(() -> new RuntimeException("Error: Rol 'Familiar' no encontrado."));
        familiar.setRol(rolFamiliar);

        if (familiar.getTelefonos() != null) {
            familiar.getTelefonos().forEach(telefono -> telefono.setUsuario(familiar));
        }
        familiar.setNecesitaCambioContrasena(true);
        if (familiar.getEstado() == null) {
            familiar.setEstado(EstadoUsuario.Activo);
        }
        return toDTO(usuarioRepository.save(familiar));
    }

    private void actualizarTelefonos(Usuario usuarioExistente, List<Telefono> nuevosTelefonos) {
        if (usuarioExistente.getTelefonos() == null) {
            usuarioExistente.setTelefonos(new ArrayList<>());
        } else {
            usuarioExistente.getTelefonos().clear();
        }
        if (nuevosTelefonos != null) {
            nuevosTelefonos.forEach(tel -> {
                tel.setUsuario(usuarioExistente);
                usuarioExistente.getTelefonos().add(tel);
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
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));

        if (usuario instanceof Cuidador) {
            List<PacienteAsignado> asignacionesActivas = pacienteAsignadoRepository.findByCuidador_idUsuarioAndEstado(id, EstadoAsignacion.Activo);
            if (!asignacionesActivas.isEmpty()) {
                throw new IllegalStateException("No se puede eliminar el cuidador porque tiene pacientes asignados.");
            }
        }

        if (usuario instanceof Familiar) {
            List<PacienteAsignado> asignaciones = pacienteAsignadoRepository.findByFamiliar_idUsuario(id);
            if (!asignaciones.isEmpty()) {
                asignaciones.forEach(asignacion -> asignacion.setFamiliar(null));
                pacienteAsignadoRepository.saveAll(asignaciones);
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
    public Optional<UsuarioDTO> actualizarUsuario(Long id, UsuarioDTO usuarioDTO) {
        return usuarioRepository.findById(id).map(usuarioExistente -> {
            String oldEmail = usuarioExistente.getCorreoElectronico();
            String newEmail = usuarioDTO.getCorreoElectronico();
            boolean emailChanged = false;

            if (newEmail != null && !newEmail.equalsIgnoreCase(oldEmail)) {
                if (usuarioRepository.findByCorreoElectronico(newEmail).isPresent()) {
                    throw new IllegalStateException("El correo " + newEmail + " ya está en uso.");
                }
                usuarioExistente.setCorreoElectronico(newEmail);
                emailChanged = true;
            }

            usuarioExistente.setNombre(usuarioDTO.getNombre());
            usuarioExistente.setApellido(usuarioDTO.getApellido());
            usuarioExistente.setDireccion(usuarioDTO.getDireccion());

            if (usuarioExistente instanceof Empleado) {
                Empleado empleadoExistente = (Empleado) usuarioExistente;
                empleadoExistente.setTipoContrato(usuarioDTO.getTipoContrato());
                empleadoExistente.setContactoEmergencia(usuarioDTO.getContactoEmergencia());
            }

            if (usuarioDTO.getTelefonos() != null) {
                usuarioExistente.getTelefonos().clear();
                List<Telefono> nuevosTelefonos = usuarioDTO.getTelefonos().stream()
                        .filter(numero -> numero != null && !numero.trim().isEmpty())
                        .map(numero -> {
                            Telefono tel = new Telefono();
                            tel.setNumero(numero);
                            tel.setUsuario(usuarioExistente);
                            return tel;
                        })
                        .collect(Collectors.toList());
                usuarioExistente.getTelefonos().addAll(nuevosTelefonos);
            }

            Usuario usuarioActualizado = usuarioRepository.save(usuarioExistente);
            if (emailChanged) {
                emailService.sendEmailChangeNotification(usuarioActualizado.getCorreoElectronico(), usuarioActualizado.getNombre());
            }
            return toDTO(usuarioActualizado);
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

    // =========================================================================
    // 🟢 AQUÍ ESTÁ LA LÓGICA DE RECUPERACIÓN DE CONTRASEÑA CORREGIDA
    // =========================================================================
    @Override
    public void createPasswordResetTokenForUser(String email) {
        Usuario usuario = usuarioRepository.findByCorreoElectronico(email)
                .orElseThrow(() -> new RuntimeException("No se encontró usuario con correo: " + email));
        
        String token = UUID.randomUUID().toString();
        usuario.setResetPasswordToken(token);
        usuario.setResetPasswordTokenExpiryDate(LocalDateTime.now().plusHours(1));
        usuarioRepository.save(usuario);

        // CONSTRUCCIÓN SEGURA DE LA URL
        // Usamos la variable 'baseUrl' inyectada desde application.properties
        String resetUrl = baseUrl + "/reset-password?token=" + token;

        // IMPORTANTE:
        // Asegúrate de que tu EmailService acepte la URL completa en el segundo parámetro.
        // Si tu EmailService solo aceptaba el token, deberás actualizarlo para que use esta URL.
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
    public void changeUserPassword(String token, String newPassword) {
        Usuario usuario = usuarioRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido."));
        if (passwordEncoder.matches(newPassword, usuario.getContrasena())) {
            throw new IllegalStateException("La nueva contraseña no puede ser igual a la anterior.");
        }
        usuario.setContrasena(passwordEncoder.encode(newPassword));
        usuario.setResetPasswordToken(null);
        usuario.setResetPasswordTokenExpiryDate(null);
        usuario.setNecesitaCambioContrasena(false);
        usuarioRepository.save(usuario);
    }

    // =========================================================================
    // MÉTODOS DE SOPORTE Y EXPORTACIÓN
    // =========================================================================

    private UsuarioDTO toDTO(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        setCommonProperties(usuario, dto);
        if (usuario instanceof Empleado) {
            Empleado empleado = (Empleado) usuario;
            dto.setFechaContratacion(empleado.getFechaContratacion());
            dto.setTipoContrato(empleado.getTipoContrato());
            dto.setContactoEmergencia(empleado.getContactoEmergencia());
            dto.setFechaNacimiento(empleado.getFechaNacimiento());
        } else if (usuario instanceof Familiar) {
            Familiar familiar = (Familiar) usuario;
            dto.setParentesco(familiar.getParentesco());
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

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    public void exportarUsuariosAExcel(OutputStream outputStream, String nombre, String documento, RolNombre rol) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Usuarios");

            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Tipo Doc", "Documento", "Nombre", "Apellido", "Dirección", "Correo", "Rol"};

            for(int i=0; i<columns.length; i++) {
                headerRow.createCell(i).setCellValue(columns[i]);
            }

            List<Usuario> usuarios = usuarioRepository.findAll(UsuarioSpecification.findByCriteria(nombre, documento, rol));

            int rowNum = 1;
            for (Usuario usuario : usuarios) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(usuario.getIdUsuario());
                row.createCell(1).setCellValue(usuario.getTipoDocumento() != null ? usuario.getTipoDocumento().toString() : "");
                row.createCell(2).setCellValue(usuario.getDocumentoIdentificacion());
                row.createCell(3).setCellValue(usuario.getNombre());
                row.createCell(4).setCellValue(usuario.getApellido());
                row.createCell(5).setCellValue(usuario.getDireccion());
                row.createCell(6).setCellValue(usuario.getCorreoElectronico());
                row.createCell(7).setCellValue(usuario.getRol() != null ? usuario.getRol().getRolNombre().toString() : "");
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
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
        for(String header : headers) {
            table.addCell(header);
        }

        List<Usuario> usuarios = usuarioRepository.findAll(UsuarioSpecification.findByCriteria(nombre, documento, rol));

        for (Usuario usuario : usuarios) {
            table.addCell(String.valueOf(usuario.getIdUsuario()));
            table.addCell(usuario.getTipoDocumento() != null ? usuario.getTipoDocumento().toString() : "");
            table.addCell(usuario.getDocumentoIdentificacion());
            table.addCell(usuario.getNombre());
            table.addCell(usuario.getApellido());
            table.addCell(usuario.getDireccion());
            table.addCell(usuario.getCorreoElectronico());
            table.addCell(usuario.getRol() != null ? usuario.getRol().getRolNombre().toString() : "");
        }
        document.add(table);
        document.close();
    }
}
