package com.example.Gericare.Controller;

import com.example.Gericare.DTO.UsuarioDTO;
import com.example.Gericare.Impl.UsuarioServiceImpl;
import com.example.Gericare.Service.EmailService;
import com.example.Gericare.Service.UsuarioService;
import com.example.Gericare.Entity.*;
import com.example.Gericare.Enums.RolNombre;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private UsuarioServiceImpl usuarioServiceImpl;
    @Autowired
    private EmailService emailService;

    @GetMapping("/editar/{id}")
    public String mostrarFormularioDeEdicion(@PathVariable Long id, Model model) {
        usuarioService.obtenerUsuarioPorId(id).ifPresent(usuario -> {
            model.addAttribute("usuario", usuario);
        });
        model.addAttribute("adminMode", true);
        return "usuario/perfil";
    }

    @PostMapping("/editar/{id}")
    public String actualizarUsuario(@PathVariable Long id, @ModelAttribute("usuario") UsuarioDTO usuarioDTO, RedirectAttributes redirectAttributes) {
        usuarioService.actualizarUsuario(id, usuarioDTO);
        redirectAttributes.addFlashAttribute("successMessage", "¡Usuario actualizado con éxito!");
        return "redirect:/dashboard";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.eliminarUsuario(id);
            redirectAttributes.addFlashAttribute("successMessage", "¡Usuario eliminado con éxito!");
        } catch (IllegalStateException e) {
            // Capturar error específico si se intenta eliminar un cuidador con pacientes
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            // Capturar cualquier otro error inesperado
            redirectAttributes.addFlashAttribute("errorMessage", "Ocurrió un error al intentar eliminar el usuario.");
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/exportExcel")
    public ResponseEntity<InputStreamResource> exportarExcel(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String documento,
            @RequestParam(required = false) RolNombre rol) throws IOException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        usuarioServiceImpl.exportarUsuariosAExcel(outputStream, nombre, documento, rol);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "usuarios.xlsx");
        return new ResponseEntity<>(new InputStreamResource(new ByteArrayInputStream(outputStream.toByteArray())), headers, HttpStatus.OK);
    }

    @GetMapping("/exportPdf")
    public ResponseEntity<InputStreamResource> exportarPdf(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String documento,
            @RequestParam(required = false) RolNombre rol) throws IOException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        usuarioServiceImpl.exportarUsuariosAPDF(outputStream, nombre, documento, rol);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "users.pdf");
        return new ResponseEntity<>(new InputStreamResource(new ByteArrayInputStream(outputStream.toByteArray())), headers, HttpStatus.OK);
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevoUsuario(Model model) {
        if (!model.containsAttribute("usuario")) {
            model.addAttribute("usuario", new UsuarioDTO());
        }
        model.addAttribute("roles", new RolNombre[]{RolNombre.Cuidador, RolNombre.Familiar});
        return "usuario/admin-formulario-usuario";
    }

    @PostMapping("/crear")
    public String crearUsuario(@Valid @ModelAttribute("usuario") UsuarioDTO usuarioDTO, BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {

        // Validar si hay errores (excluyendo contraseña)
        if (bindingResult.hasErrors()) {
            // Comprobar si los únicos errores son de contraseña
            boolean onlyPasswordErrors = bindingResult.getFieldErrors().stream()
                    .allMatch(fe -> fe.getField().equals("contrasena"));

            // Si hay otros errores O si hay errores de contraseña y mas errores
            if (!onlyPasswordErrors || bindingResult.getErrorCount() > bindingResult.getFieldErrorCount("contrasena")) {
                redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.usuario", bindingResult);
                redirectAttributes.addFlashAttribute("usuario", usuarioDTO);
                return "redirect:/usuarios/nuevo";
            }
        }


        try {
            RolNombre rolSeleccionado = usuarioDTO.getRol().getRolNombre();
            String emailUsuario = usuarioDTO.getCorreoElectronico();
            String nombreUsuario = usuarioDTO.getNombre();
            String documento = usuarioDTO.getDocumentoIdentificacion(); // Contraseña inicial

            List<Telefono> telefonos = new ArrayList<>();
            if (usuarioDTO.getTelefonos() != null) {
                telefonos = usuarioDTO.getTelefonos().stream()
                        .filter(numero -> numero != null && !numero.trim().isEmpty())
                        .map(numero -> {
                            Telefono tel = new Telefono();
                            tel.setNumero(numero);
                            return tel;
                        })
                        .collect(Collectors.toList());
            }

            if (rolSeleccionado == RolNombre.Cuidador) {
                Cuidador cuidador = new Cuidador();
                // Copiar datos DTO -> Entidad
                cuidador.setTipoDocumento(usuarioDTO.getTipoDocumento());
                cuidador.setDocumentoIdentificacion(usuarioDTO.getDocumentoIdentificacion());
                cuidador.setNombre(usuarioDTO.getNombre());
                cuidador.setApellido(usuarioDTO.getApellido());
                cuidador.setDireccion(usuarioDTO.getDireccion());
                cuidador.setCorreoElectronico(usuarioDTO.getCorreoElectronico());
                cuidador.setContrasena(usuarioDTO.getContrasena()); // Contiene el documento
                cuidador.setFechaContratacion(usuarioDTO.getFechaContratacion());
                cuidador.setTipoContrato(usuarioDTO.getTipoContrato());
                cuidador.setContactoEmergencia(usuarioDTO.getContactoEmergencia());
                cuidador.setFechaNacimiento(usuarioDTO.getFechaNacimiento());
                cuidador.setTelefonos(telefonos);
                telefonos.forEach(tel -> tel.setUsuario(cuidador)); // Relación bidireccional

                usuarioService.crearCuidador(cuidador); // Llamada al service

            } else if (rolSeleccionado == RolNombre.Familiar) {
                Familiar familiar = new Familiar();
                // Copiar datos DTO -> Entidad
                familiar.setTipoDocumento(usuarioDTO.getTipoDocumento());
                familiar.setDocumentoIdentificacion(usuarioDTO.getDocumentoIdentificacion());
                familiar.setNombre(usuarioDTO.getNombre());
                familiar.setApellido(usuarioDTO.getApellido());
                familiar.setDireccion(usuarioDTO.getDireccion());
                familiar.setCorreoElectronico(usuarioDTO.getCorreoElectronico());
                familiar.setContrasena(usuarioDTO.getContrasena()); // Contiene el documento
                familiar.setParentesco(usuarioDTO.getParentesco());
                familiar.setTelefonos(telefonos);
                telefonos.forEach(tel -> tel.setUsuario(familiar)); // Relación bidireccional

                usuarioService.crearFamiliar(familiar); // Llamada al service
            } else {
                throw new IllegalArgumentException("El rol seleccionado no es válido para la creación.");
            }

            // Llamada para enviar correo de bienvenida
            try {
                emailService.sendWelcomeEmail(emailUsuario, nombreUsuario, documento);
            } catch (Exception mailException) {
                // Loggear el error de envío de correo, pero no detener el flujo principal
                System.err.println("Usuario creado. Falló el envío del correo de bienvenida a " + emailUsuario + ": " + mailException.getMessage());
            }

            redirectAttributes.addFlashAttribute("successMessage", "¡" + nombreUsuario + " " + usuarioDTO.getApellido() + " se ha registrado correctamente!");
            return "redirect:/dashboard";

        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ya existe un usuario con el mismo documento o correo electrónico.");
            redirectAttributes.addFlashAttribute("usuario", usuarioDTO);
            return "redirect:/usuarios/nuevo";
        } catch (Exception e) {
            System.err.println("Error general creando usuario: " + e.getMessage()); // Loggear error
            redirectAttributes.addFlashAttribute("errorMessage", "Error al crear el usuario: " + e.getMessage());
            redirectAttributes.addFlashAttribute("usuario", usuarioDTO);
            return "redirect:/usuarios/nuevo";
        }
    }
}