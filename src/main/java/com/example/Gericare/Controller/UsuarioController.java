package com.example.Gericare.Controller;

import com.example.Gericare.DTO.UsuarioDTO;
import com.example.Gericare.Impl.UsuarioServiceImpl;
import com.example.Gericare.Service.UsuarioService;
import com.example.Gericare.entity.Cuidador;
import com.example.Gericare.entity.Familiar;
import com.example.Gericare.entity.Telefono;
import com.example.Gericare.enums.RolNombre;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
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

    @GetMapping("/editar/{id}")
    public String mostrarFormularioDeEdicion(@PathVariable Long id, Model model) {
        usuarioService.obtenerUsuarioPorId(id).ifPresent(usuario -> {
            model.addAttribute("usuario", usuario);
        });
        model.addAttribute("adminMode", true);
        return "formulario-usuario-editar";
    }

    @PostMapping("/editar/{id}")
    public String actualizarUsuario(@PathVariable Long id, @ModelAttribute("usuario") UsuarioDTO usuarioDTO) {
        usuarioService.actualizarUsuario(id, usuarioDTO);
        return "redirect:/dashboard";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
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
        return "formulario-usuario";
    }

    @PostMapping("/crear")
    public String crearUsuario(@Valid @ModelAttribute("usuario") UsuarioDTO usuarioDTO, BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.usuario", bindingResult);
            redirectAttributes.addFlashAttribute("usuario", usuarioDTO);
            return "redirect:/usuarios/nuevo";
        }
        try {
            RolNombre rolSeleccionado = usuarioDTO.getRol().getRolNombre();

            // Convertir la lista de strings de teléfonos a una lista de entidades Telefono
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
                cuidador.setTipoDocumento(usuarioDTO.getTipoDocumento());
                cuidador.setDocumentoIdentificacion(usuarioDTO.getDocumentoIdentificacion());
                cuidador.setNombre(usuarioDTO.getNombre());
                cuidador.setApellido(usuarioDTO.getApellido());
                cuidador.setDireccion(usuarioDTO.getDireccion());
                cuidador.setCorreoElectronico(usuarioDTO.getCorreoElectronico());
                cuidador.setContrasena(usuarioDTO.getContrasena());
                cuidador.setFechaContratacion(usuarioDTO.getFechaContratacion());
                cuidador.setTipoContrato(usuarioDTO.getTipoContrato());
                cuidador.setContactoEmergencia(usuarioDTO.getContactoEmergencia());
                cuidador.setFechaNacimiento(usuarioDTO.getFechaNacimiento());
                // Asignar teléfonos y establecer la relación bidireccional
                cuidador.setTelefonos(telefonos);
                telefonos.forEach(tel -> tel.setUsuario(cuidador));

                usuarioService.crearCuidador(cuidador);

            } else if (rolSeleccionado == RolNombre.Familiar) {
                Familiar familiar = new Familiar();
                familiar.setTipoDocumento(usuarioDTO.getTipoDocumento());
                familiar.setDocumentoIdentificacion(usuarioDTO.getDocumentoIdentificacion());
                familiar.setNombre(usuarioDTO.getNombre());
                familiar.setApellido(usuarioDTO.getApellido());
                familiar.setDireccion(usuarioDTO.getDireccion());
                familiar.setCorreoElectronico(usuarioDTO.getCorreoElectronico());
                familiar.setContrasena(usuarioDTO.getContrasena());
                familiar.setParentesco(usuarioDTO.getParentesco());
                // Asignar teléfonos y establecer la relación bidireccional
                familiar.setTelefonos(telefonos);
                telefonos.forEach(tel -> tel.setUsuario(familiar));

                usuarioService.crearFamiliar(familiar);
            } else {
                throw new IllegalArgumentException("El rol seleccionado no es válido para la creación.");
            }

            redirectAttributes.addFlashAttribute("success", "¡" + usuarioDTO.getNombre() + " " + usuarioDTO.getApellido() + " se ha registrado correctamente!");
            return "redirect:/dashboard";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear el usuario: " + e.getMessage());
            redirectAttributes.addFlashAttribute("usuario", usuarioDTO);
            return "redirect:/usuarios/nuevo";
        }
    }
}