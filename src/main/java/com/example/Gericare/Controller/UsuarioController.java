package com.example.Gericare.Controller;

import com.example.Gericare.DTO.UsuarioDTO;
import com.example.Gericare.Impl.UsuarioServiceImpl;
import com.example.Gericare.Service.UsuarioService;
import com.example.Gericare.entity.Cuidador;
import com.example.Gericare.entity.Familiar;
import com.example.Gericare.entity.Rol;
import com.example.Gericare.enums.RolNombre;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private UsuarioServiceImpl usuarioServiceImpl;

    // Metodos para las vistas

    // Formulario para editar un usuario
    // En UsuarioController.java

    @GetMapping("/editar/{id}")
    public String mostrarFormularioDeEdicion(@PathVariable Long id, Model model) {
        usuarioService.obtenerUsuarioPorId(id).ifPresent(usuario -> {
            model.addAttribute("usuario", usuario);
        });
        model.addAttribute("adminMode", true);
        return "formulario-usuario-editar";
    }

    // Actualización usuario
    @PostMapping("/editar/{id}")
    public String actualizarUsuario(@PathVariable Long id, @Valid @ModelAttribute("usuario") UsuarioDTO usuarioDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "formulario-usuario-editar";
        }
        usuarioService.actualizarUsuario(id, usuarioDTO);
        return "redirect:/dashboard";
    }

    // Borrar usuario
    @PostMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return "redirect:/dashboard";
    }


    // Exportar datos pdf y excel

    @GetMapping("/exportExcel")
    public ResponseEntity<InputStreamResource> exportarExcel(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String documento,
            @RequestParam(required = false) RolNombre rol) throws IOException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // Pasamos los filtros al servicio
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
        // Pasamos los filtros al servicio
        usuarioServiceImpl.exportarUsuariosAPDF(outputStream, nombre, documento, rol);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "users.pdf");
        return new ResponseEntity<>(new InputStreamResource(new ByteArrayInputStream(outputStream.toByteArray())), headers, HttpStatus.OK);
    }

    //Nuevos metodos para crear usuario

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevoUsuario(Model model) {
        if (!model.containsAttribute("usuario")) {
            model.addAttribute("usuario", new UsuarioDTO());
        }
        model.addAttribute("roles", new RolNombre[]{RolNombre.Cuidador, RolNombre.Familiar});
        return "formulario-usuario"; // Vista para crear usuarios
    }

    @PostMapping("/crear")
    public String crearUsuario(@Valid @ModelAttribute("usuario") UsuarioDTO usuarioDTO, BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            // Si hay errores, volvemos al formulario para mostrarlos
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.usuario", bindingResult);
            redirectAttributes.addFlashAttribute("usuario", usuarioDTO);
            return "redirect:/usuarios/nuevo";
        }
        try {
            // Se obtiene el RolNombre del objeto Rol dentro del DTO
            RolNombre rolSeleccionado = usuarioDTO.getRol().getRolNombre();

            if (rolSeleccionado == RolNombre.Cuidador) {
                Cuidador cuidador = new Cuidador();
                // --- Mapeo completo para Cuidador ---
                cuidador.setTipoDocumento(usuarioDTO.getTipoDocumento());
                cuidador.setDocumentoIdentificacion(usuarioDTO.getDocumentoIdentificacion());
                cuidador.setNombre(usuarioDTO.getNombre());
                cuidador.setApellido(usuarioDTO.getApellido());
                cuidador.setDireccion(usuarioDTO.getDireccion());
                cuidador.setCorreoElectronico(usuarioDTO.getCorreoElectronico());
                cuidador.setContrasena(usuarioDTO.getContrasena());
                // Campos específicos de Empleado
                cuidador.setFechaContratacion(usuarioDTO.getFechaContratacion());
                cuidador.setTipoContrato(usuarioDTO.getTipoContrato());
                cuidador.setContactoEmergencia(usuarioDTO.getContactoEmergencia());
                cuidador.setFechaNacimiento(usuarioDTO.getFechaNacimiento());

                usuarioService.crearCuidador(cuidador);

            } else if (rolSeleccionado == RolNombre.Familiar) {
                Familiar familiar = new Familiar();
                // --- Mapeo completo para Familiar ---
                familiar.setTipoDocumento(usuarioDTO.getTipoDocumento());
                familiar.setDocumentoIdentificacion(usuarioDTO.getDocumentoIdentificacion());
                familiar.setNombre(usuarioDTO.getNombre());
                familiar.setApellido(usuarioDTO.getApellido());
                familiar.setDireccion(usuarioDTO.getDireccion());
                familiar.setCorreoElectronico(usuarioDTO.getCorreoElectronico());
                familiar.setContrasena(usuarioDTO.getContrasena());
                // Campo específico de Familiar
                familiar.setParentesco(usuarioDTO.getParentesco());

                usuarioService.crearFamiliar(familiar);
            } else {
                throw new IllegalArgumentException("El rol seleccionado no es válido para la creación.");
            }

            redirectAttributes.addFlashAttribute("success", "¡Usuario creado con éxito!");
            return "redirect:/dashboard";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear el usuario: " + e.getMessage());
            redirectAttributes.addFlashAttribute("usuario", usuarioDTO);
            return "redirect:/usuarios/nuevo";
        }
    }
}