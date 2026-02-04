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

// Imports para Excel (Apache POI) - ESTOS AHORA SÍ FUNCIONARÁN
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

    // Envío de correos masivos por Rol
    @Override
    public void sendCustomBulkEmailToRole(RolNombre role, String subject, String body) {

        // crea una lista vacía que almacena los usuarios a los que se les enviará correo
        List<Usuario> targetUsers = new ArrayList<>();

        // valida si el rol recibido es null, lo que indica que se envía a familiares y cuidadores
        if (role == null) {
            // obtiene todos los usuarios con rol familiar y los agrega a la lista
            targetUsers.addAll(usuarioRepository.findByRol_RolNombre(RolNombre.Familiar));
            // obtiene todos los usuarios con rol cuidador y los agrega a la lista
            targetUsers.addAll(usuarioRepository.findByRol_RolNombre(RolNombre.Cuidador));
        } else {
            // cuando se recibe un rol específico, obtiene solo los usuarios de ese rol
            targetUsers = usuarioRepository.findByRol_RolNombre(role);
        }

        // convierte la lista de usuarios en una lista de correos electrónicos usando stream
        List<String> recipientEmails = targetUsers.stream()
                .map(Usuario::getCorreoElectronico) // obtiene el correo de cada usuario
                .collect(Collectors.toList()); // convierte en lista de strings

        // verifica que exista al menos un correo antes de proceder
        if (!recipientEmails.isEmpty()) {
            // llama al servicio de correo para que ejecute el envío masivo en modo bcc
            emailService.sendBulkEmail(recipientEmails, subject, body);
        }
    }

    @Override
    @Transactional
    public UsuarioDTO crearCuidador(Cuidador cuidador) {
        // Verificar si ya existe (tmb inactivo) usando el método del repositorio
        Optional<Usuario> usuarioExistente = usuarioRepository.findByDocumentoOrEmailNative(
                cuidador.getDocumentoIdentificacion(),
                cuidador.getCorreoElectronico()
        );

        if (usuarioExistente.isPresent()) {
            Usuario existente = usuarioExistente.get();

            // Si existe y está act -> Error, ya existe.
            if (existente.getEstado() == EstadoUsuario.Activo) {
                throw new DataIntegrityViolationException("El usuario ya existe con ese documento o correo.");
            }

            // Si existe, está inactivo y es Cuidador lo recupera
            if (existente instanceof Cuidador) {
                Cuidador cuidadorRecuperado = (Cuidador) existente;

                // Actualiza sus datos básicos
                cuidadorRecuperado.setNombre(cuidador.getNombre());
                cuidadorRecuperado.setApellido(cuidador.getApellido());
                cuidadorRecuperado.setDireccion(cuidador.getDireccion());
                cuidadorRecuperado.setCorreoElectronico(cuidador.getCorreoElectronico());

                // Encripta la nueva contraseña
                cuidadorRecuperado.setContrasena(passwordEncoder.encode(cuidador.getContrasena()));

                // Actualiza datos específicos de Cuidador
                cuidadorRecuperado.setFechaContratacion(cuidador.getFechaContratacion());
                cuidadorRecuperado.setTipoContrato(cuidador.getTipoContrato());
                cuidadorRecuperado.setContactoEmergencia(cuidador.getContactoEmergencia());
                cuidadorRecuperado.setFechaNacimiento(cuidador.getFechaNacimiento());

                // Actualiza teléfonos (usando método auxiliar)
                actualizarTelefonos(cuidadorRecuperado, cuidador.getTelefonos());

                // Reactiva el usuario
                cuidadorRecuperado.setEstado(EstadoUsuario.Activo);
                cuidadorRecuperado.setNecesitaCambioContrasena(true);

                return toDTO(usuarioRepository.save(cuidadorRecuperado));
            } else {
                // Existe pero era Familiar y ahora intenta ser Cuidador (conflicto de roles)
                throw new DataIntegrityViolationException("El documento ya existe registrado con otro rol y no se puede recuperar.");
            }
        }

        // Flujo normal de creación
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

        // Verificar si ya existe (tmb inactivo)
        Optional<Usuario> usuarioExistente = usuarioRepository.findByDocumentoOrEmailNative(
                familiar.getDocumentoIdentificacion(),
                familiar.getCorreoElectronico()
        );

        if (usuarioExistente.isPresent()) {
            Usuario existente = usuarioExistente.get();

            if (existente.getEstado() == EstadoUsuario.Activo) {
                throw new DataIntegrityViolationException("El usuario ya existe con ese documento o correo.");
            }

            // Si existe, está inactivo y es Familiar recupera
            if (existente instanceof Familiar) {
                Familiar familiarRecuperado = (Familiar) existente;

                // Actualiza datos básicos
                familiarRecuperado.setNombre(familiar.getNombre());
                familiarRecuperado.setApellido(familiar.getApellido());
                familiarRecuperado.setDireccion(familiar.getDireccion());
                familiarRecuperado.setCorreoElectronico(familiar.getCorreoElectronico());

                // Encripta la nueva contraseña
                familiarRecuperado.setContrasena(passwordEncoder.encode(familiar.getContrasena()));

                // Datos específicos
                familiarRecuperado.setParentesco(familiar.getParentesco());

                // Actualiza teléfonos
                actualizarTelefonos(familiarRecuperado, familiar.getTelefonos());

                // Reactiva
                familiarRecuperado.setEstado(EstadoUsuario.Activo);
                familiarRecuperado.setNecesitaCambioContrasena(true);

                return toDTO(usuarioRepository.save(familiarRecuperado));
            } else {
                throw new DataIntegrityViolationException("El documento ya existe registrado con otro rol.");
            }
        }

        // flujo creación normal
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

    // método auxiliar actualizar tel
    private void actualizarTelefonos(Usuario usuarioExistente, List<Telefono> nuevosTelefonos) {
        // Limpia la lista actual de teléfonos
        if (usuarioExistente.getTelefonos() == null) {
            usuarioExistente.setTelefonos(new ArrayList<>());
        } else {
            usuarioExistente.getTelefonos().clear();
        }

        // Asigna los nuevos (si hay) y vincula
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

   @Override
public void createPasswordResetTokenForUser(String email) {
    // Usamos el repositorio que ya tienes
    Usuario usuario = usuarioRepository.findByCorreoElectronico(email)
            .orElseThrow(() -> new RuntimeException("No se encontró usuario con correo: " + email));
    
    String token = UUID.randomUUID().toString();
    usuario.setResetPasswordToken(token);
    usuario.setResetPasswordTokenExpiryDate(LocalDateTime.now().plusHours(1));
    
    usuarioRepository.save(usuario);

    // IMPORTANTE: El emailService debe recibir el link completo para el HTML
    String resetUrl = "http://localhost:8080/reset-password?token=" + token;
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

    // Hasheamos y quitamos la bandera de "primera vez"
    usuario.setContrasena(passwordEncoder.encode(newPassword));
    usuario.setResetPasswordToken(null);
    usuario.setResetPasswordTokenExpiryDate(null);
    
    // AQUÍ ESTÁ LA MAGIA: Ya no le pedirá cambiarla al entrar
    usuario.setNecesitaCambioContrasena(false); 
    
    usuarioRepository.save(usuario);
}

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

    // exportacion excel
    public void exportarUsuariosAExcel(OutputStream outputStream, String nombre, String documento, RolNombre rol) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) { // XSSFWorkbook requiere poi-ooxml
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

    // exportacion pdf
    public void exportarUsuariosAPDF(OutputStream outputStream, String nombre, String documento, RolNombre rol) throws IOException {
        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);
        document.open();

        document.add(new Paragraph("Lista de Usuarios - Gericare Connect"));
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);

        // Headers
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
