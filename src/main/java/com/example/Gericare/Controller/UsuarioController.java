package com.example.Gericare.Controller;

import com.example.Gericare.DTO.UsuarioDTO;
import com.example.Gericare.Impl.UsuarioServiceImpl;
import com.example.Gericare.Service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping("/editar/{id}")
    public String mostrarFormularioDeEdicion(@PathVariable Long id, Model model) {
        usuarioService.obtenerUsuarioPorId(id).ifPresent(usuario -> {
            model.addAttribute("usuario", usuario);
        });
        // Devuelve la vista que se creo
        return "formulario-usuario-editar";
    }

    // Actualización usuario
    @PostMapping("/editar/{id}")
    public String actualizarUsuario(@PathVariable Long id, @ModelAttribute("usuario") UsuarioDTO usuarioDTO) {
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
    public ResponseEntity<InputStreamResource> exportarExcel() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        usuarioServiceImpl.exportarUsuariosAExcel(outputStream);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "users.xlsx");
        return new ResponseEntity<>(new InputStreamResource(new ByteArrayInputStream(outputStream.toByteArray())), headers, HttpStatus.OK);
    }

    @GetMapping("/exportPdf")
    public ResponseEntity<InputStreamResource> exportarPdf() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        usuarioServiceImpl.exportarUsuariosAPDF(outputStream);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "users.pdf");
        return new ResponseEntity<>(new InputStreamResource(new ByteArrayInputStream(outputStream.toByteArray())), headers, HttpStatus.OK);
    }
}