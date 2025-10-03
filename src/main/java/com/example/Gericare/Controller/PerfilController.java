package com.example.Gericare.Controller;

import com.example.Gericare.DTO.UsuarioDTO;
import com.example.Gericare.Service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public String mostrarFormularioPerfil(Authentication authentication, Model model) {
        String userEmail = authentication.getName();
        usuarioService.findByEmail(userEmail).ifPresent(usuario -> {
            model.addAttribute("usuario", usuario);
        });
        return "formulario-usuario-editar";
    }

    @PostMapping
    public String actualizarPerfil(Authentication authentication, @ModelAttribute("usuario") UsuarioDTO usuarioDTO) {
        usuarioService.findByEmail(authentication.getName()).ifPresent(usuarioExistente -> {
            usuarioService.actualizarUsuario(usuarioExistente.getIdUsuario(), usuarioDTO);
        });
        return "redirect:/dashboard?perfilActualizado=true";
    }

    @PostMapping("/solicitar-cambio-password")
    public String solicitarCambioPassword(Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            // Llama al servicio "usuarioService.createPasswordResetTokenForUser()" para que haga el trabajo,
            // después el controlador delega la responsabilidad al servicio UsuarioServiceImpl
            usuarioService.createPasswordResetTokenForUser(authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Se ha enviado un correo con las instrucciones para cambiar la contraseña.");
        } catch (Exception e) {
            // Manejo de errores
            redirectAttributes.addFlashAttribute("errorMessage", "No se pudo procesar la solicitud. Inténtalo de nuevo.");
        }
        return "redirect:/perfil";
    }
}