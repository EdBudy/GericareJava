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
import com.example.Gericare.Entity.Usuario;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import java.util.ArrayList; // Necesario para el nuevo metodo de masivos

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
    @Lazy // Se usa @Lazy para romper una posible dependencia circular al inyectar servicios entre sí
    private PacienteAsignadoService pacienteAsignadoService;
    @Autowired
    private EmailService emailService;

    // metodo para enviar correos masivos personalizados
    @Override
    public void sendCustomBulkEmailToRole(RolNombre role, String subject, String body) {
        List<Usuario> targetUsers = new ArrayList<>();

        if (role == null) { // Si el rol es null, asumimos que es para "Todos" (Familiares y Cuidadores)
            targetUsers.addAll(usuarioRepository.findByRol_RolNombre(RolNombre.Familiar)); //
            targetUsers.addAll(usuarioRepository.findByRol_RolNombre(RolNombre.Cuidador)); //
        } else {
            targetUsers = usuarioRepository.findByRol_RolNombre(role); //
        }

        // Obtener solo los correos electrónicos
        List<String> recipientEmails = targetUsers.stream()
                .map(Usuario::getCorreoElectronico) //
                .collect(Collectors.toList()); //

        if (!recipientEmails.isEmpty()) {
            emailService.sendBulkEmail(recipientEmails, subject, body); // Llamada al método genérico
        } else {
            System.out.println("No se encontraron usuarios del rol " + (role != null ? role.name() : "TODOS") + " para enviar correo masivo."); //
        }
    }


    @Override
    @Transactional // Buena práctica añadir "@Transactional" para asegurar consistencia
    public UsuarioDTO crearCuidador(Cuidador cuidador) {
        // Codificar contraseña (que ya viene del controlador es el documento)
        cuidador.setContrasena(passwordEncoder.encode(cuidador.getContrasena()));

        // Obtener y asignar el Rol 'Cuidador'
        Rol rolCuidador = rolRepository.findByRolNombre(RolNombre.Cuidador)
                .orElseThrow(() -> new RuntimeException("Error: Rol 'Cuidador' no encontrado."));
        cuidador.setRol(rolCuidador);

        // Asegurar la relación bidireccional con los Teléfonos (Importante para que JPA guarde correctamente la relación)
        if (cuidador.getTelefonos() != null) {
            cuidador.getTelefonos().forEach(telefono -> telefono.setUsuario(cuidador));
        }

        // Asegurar que la bandera esté en true (aunque ya es default)
        cuidador.setNecesitaCambioContrasena(true);

        // Asegurar que el estado inicial sea Activo
        if (cuidador.getEstado() == null) {
            cuidador.setEstado(EstadoUsuario.Activo);
        }

        // Guardar cuidador en bd
        Cuidador cuidadorGuardado = usuarioRepository.save(cuidador);

        // Convertir la entidad guardada a DTO y retornarla
        return toDTO(cuidadorGuardado);
    }

    @Override
    @Transactional
    public UsuarioDTO crearFamiliar(Familiar familiar) {
        // Validación de cantidad de teléfonos
        if (familiar.getTelefonos() != null && familiar.getTelefonos().size() > 3) {
            throw new IllegalStateException("Un familiar no puede tener más de 3 teléfonos.");
        }

        // Codificar la contraseña
        familiar.setContrasena(passwordEncoder.encode(familiar.getContrasena()));

        // Obtener y asignar Rol 'Familiar'
        Rol rolFamiliar = rolRepository.findByRolNombre(RolNombre.Familiar)
                .orElseThrow(() -> new RuntimeException("Error: Rol 'Familiar' no encontrado."));
        familiar.setRol(rolFamiliar);

        // Asegurar la relación bidireccional con los Teléfonos
        if (familiar.getTelefonos() != null) {
            familiar.getTelefonos().forEach(telefono -> telefono.setUsuario(familiar));
        }

        // Asegurar que la bandera esté en true (aunque ya es default)
        familiar.setNecesitaCambioContrasena(true);

        // Asegurar que el estado inicial sea Activo
        if (familiar.getEstado() == null) {
            familiar.setEstado(EstadoUsuario.Activo);
        }

        // Guardar el familiar en bd
        Familiar familiarGuardado = usuarioRepository.save(familiar);

        // Convertir la entidad guardada a DTO y retornarla
        return toDTO(familiarGuardado);
    }

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

    @Override
    @Transactional
    public void eliminarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));

        // Si es un Cuidador, verifica si tiene pacientes asignados
        if (usuario instanceof Cuidador) {
            List<PacienteAsignado> asignacionesActivas = pacienteAsignadoRepository.findByCuidador_idUsuarioAndEstado(id, EstadoAsignacion.Activo);
            if (!asignacionesActivas.isEmpty()) {
                throw new IllegalStateException("No se puede eliminar el cuidador porque tiene pacientes asignados. Por favor, reasigne los pacientes a otro cuidador antes de eliminarlo.");
            }
        }

        // Si es un Familiar, busca sus asignaciones y elimina la referencia
        if (usuario instanceof Familiar) {
            List<PacienteAsignado> asignaciones = pacienteAsignadoRepository.findByFamiliar_idUsuario(id);
            if (!asignaciones.isEmpty()) {
                asignaciones.forEach(asignacion -> asignacion.setFamiliar(null));
                pacienteAsignadoRepository.saveAll(asignaciones);
            }
        }

        // Desactivar usuario
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

            // cambio de correo
            String oldEmail = usuarioExistente.getCorreoElectronico();
            String newEmail = usuarioDTO.getCorreoElectronico();
            boolean emailChanged = false; // Bandera para saber si debe enviar el correo

            if (newEmail != null && !newEmail.equalsIgnoreCase(oldEmail)) {
                // El correo cambia. Validar que el nuevo email no esté en uso
                if (usuarioRepository.findByCorreoElectronico(newEmail).isPresent()) {
                    throw new IllegalStateException("El correo " + newEmail + " ya está en uso por otra cuenta.");
                }

                // Si es válido, lo actualiza
                usuarioExistente.setCorreoElectronico(newEmail);
                emailChanged = true;
            }

            // Actualiza los campos comunes
            usuarioExistente.setNombre(usuarioDTO.getNombre());
            usuarioExistente.setApellido(usuarioDTO.getApellido());
            usuarioExistente.setDireccion(usuarioDTO.getDireccion());

            // Actualizar campos de Empleado si aplica
            if (usuarioExistente instanceof Empleado) {
                Empleado empleadoExistente = (Empleado) usuarioExistente;
                empleadoExistente.setTipoContrato(usuarioDTO.getTipoContrato());
                empleadoExistente.setContactoEmergencia(usuarioDTO.getContactoEmergencia());
            }

            // Lógica para actualizar los teléfonos
            if (usuarioDTO.getTelefonos() != null) {
                // Borrar los teléfonos antiguos para evitar duplicados
                usuarioExistente.getTelefonos().clear();

                // Crear y añadir los nuevos teléfonos desde el DTO
                List<Telefono> nuevosTelefonos = usuarioDTO.getTelefonos().stream()
                        .filter(numero -> numero != null && !numero.trim().isEmpty())
                        .map(numero -> {
                            Telefono tel = new Telefono();
                            tel.setNumero(numero);
                            tel.setUsuario(usuarioExistente); // Enlace bidireccional
                            return tel;
                        })
                        .collect(Collectors.toList());
                usuarioExistente.getTelefonos().addAll(nuevosTelefonos);
            }

            // Guarda el usuario con todos los cambios
            Usuario usuarioActualizado = usuarioRepository.save(usuarioExistente);

            // Envío de notificación/correo
            if (emailChanged) {
                emailService.sendEmailChangeNotification(
                        usuarioActualizado.getCorreoElectronico(),
                        usuarioActualizado.getNombre()
                );
            }

            return toDTO(usuarioActualizado);
        });
    }

    @Override
    public List<UsuarioDTO> findUsuariosByCriteria(String nombre, String documento, RolNombre rol) {
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
    public List<PacienteAsignadoDTO> findPacientesByFamiliarEmail(String email) {
        // Buscar en el repositorio de asignaciones por el email del familiar y el estado activo
        return pacienteAsignadoRepository.findByFamiliar_CorreoElectronicoAndEstado(email, EstadoAsignacion.Activo)
                .stream()
                .map(pacienteAsignadoService::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UsuarioDTO> findByRol(RolNombre rolNombre) {
        return usuarioRepository.findByRol_RolNombre(rolNombre)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // UsuarioServiceImpl
    // Métodos correo y token
    @Override
    public void createPasswordResetTokenForUser(String email) {
        // Encontrar usuario en la bd
        Usuario usuario = usuarioRepository.findByCorreoElectronico(email)
                .orElseThrow(() -> new RuntimeException("No se encontró un usuario con el correo: " + email));

        // Crear un token único y aleatorio usando "UUID.randomUUID().toString()" (Universally Unique Identifier)
        String token = UUID.randomUUID().toString();
        usuario.setResetPasswordToken(token);
        // Establecer una fecha de caducidad (1 hora)
        usuario.setResetPasswordTokenExpiryDate(LocalDateTime.now().plusHours(1)); // Válido por 1 hora

        // Guardar el token y la fecha en la base de datos para ese usuario
        usuarioRepository.save(usuario);

        // Llama a otro servicio para enviar el correo
        emailService.sendPasswordResetEmail(usuario.getCorreoElectronico(), token);
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
        // Buscar al usuario por el token
        Usuario usuario = usuarioRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido para cambio de contraseña."));

        if (passwordEncoder.matches(newPassword, usuario.getContrasena())) {
            throw new IllegalStateException("La nueva contraseña no puede ser igual a la anterior.");
        }

        // Encriptar la nueva contraseña
        usuario.setContrasena(passwordEncoder.encode(newPassword));
        // Invalidar el token borrándolo de la base de datos
        usuario.setResetPasswordToken(null);
        usuario.setResetPasswordTokenExpiryDate(null);

        // Marcar que la contraseña ya fue cambiada
        usuario.setNecesitaCambioContrasena(false);

        // Guardar los cambios
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

        // Teléfono a la lista de Strings del DTO
        if (usuario.getTelefonos() != null) {
            dto.setTelefonos(usuario.getTelefonos().stream()
                    .map(Telefono::getNumero)
                    .collect(Collectors.toList()));
        }

        // Campo para indicar si el usuario necesita cambiar su contraseña
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
        dto.setNecesitaCambioContrasena(usuario.isNecesitaCambioContrasena());
    }

    // ------
    // Listar todos los usuarios
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    // Exportar al excel
    public void exportarUsuariosAExcel(OutputStream outputStream, String nombre, String documento, RolNombre rol) throws IOException {
        // Libro
        Workbook workbook = new XSSFWorkbook();
        // Hoja
        Sheet sheet = workbook.createSheet("Usuarios");
        // Fila de encabezado
        Row headerRow = sheet.createRow(0);
        // Columna de encabezado
        headerRow.createCell(0).setCellValue("ID");
        headerRow.createCell(1).setCellValue("Tipo Documento");
        headerRow.createCell(2).setCellValue("Documento Identificación");
        headerRow.createCell(3).setCellValue("Nombre");
        headerRow.createCell(4).setCellValue("Apellido");
        headerRow.createCell(5).setCellValue("Dirección");
        headerRow.createCell(6).setCellValue("Correo Electrónico");
        headerRow.createCell(7).setCellValue("Rol");

        // Listar todos
        List<Usuario> usuarios = usuarioRepository.findAll(UsuarioSpecification.findByCriteria(nombre, documento, rol));

        // Llenar filas
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

        // Ajustar tamaño de las columnas
        for (int i = 0; i < 8; i++) {
            sheet.autoSizeColumn(i);
        }
        // Escribir en el OutputStream
        workbook.write(outputStream);
        workbook.close();

    }

    // Exportar a PDF
    public void exportarUsuariosAPDF(OutputStream outputStream, String nombre, String documento, RolNombre rol) throws IOException {
        // Crear documento PDF
        Document document = new Document();

        // Acosiar al OutputStream
        PdfWriter.getInstance(document, outputStream);

        // Abrir documento
        document.open();

        // Agregar contenido
        document.add(new Paragraph("Lista de Usuarios"));
        document.add(new Paragraph(" ")); // Línea en blanco

        // Crear tabla con 8 columnas
        PdfPTable table = new PdfPTable(8);

        // Agregar encabezados
        table.addCell("ID");
        table.addCell("Tipo Documento");
        table.addCell("Documento Identificación");
        table.addCell("Nombre");
        table.addCell("Apellido");
        table.addCell("Dirección");
        table.addCell("Correo Electrónico");
        table.addCell("Rol");

        // Listar todos
        List<Usuario> usuarios = usuarioRepository.findAll(UsuarioSpecification.findByCriteria(nombre, documento, rol));

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
        // Agregar tabla al documento
        document.add(table);
        // Cerrar documento
        document.close();
    }
}