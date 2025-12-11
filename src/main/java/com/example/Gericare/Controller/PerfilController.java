package com.example.Gericare.Controller;

import com.example.Gericare.DTO.UsuarioDTO;
import com.example.Gericare.Service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

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
        return "usuario/perfil";
    }

    @PostMapping
    public String actualizarPerfil(Authentication authentication,
                                   @ModelAttribute("usuario") UsuarioDTO usuarioDTO,
                                   RedirectAttributes redirectAttributes) { // Agregamos RedirectAttributes

        String oldEmail = authentication.getName();
        Optional<Long> usuarioIdOpt = usuarioService.findByEmail(oldEmail).map(UsuarioDTO::getIdUsuario);

        if (usuarioIdOpt.isPresent()) {
            try {
                // Intentar actualizar el usuario. Si el correo existe, lanzará la IllegalStateException.
                Optional<UsuarioDTO> usuarioActualizadoOpt = usuarioService.actualizarUsuario(usuarioIdOpt.get(), usuarioDTO);

                if (usuarioActualizadoOpt.isPresent()) {
                    String newEmail = usuarioActualizadoOpt.get().getCorreoElectronico();

                    // Comprobar si correo electrónico cambió y actualizar la sesión (Auth)
                    if (newEmail != null && !newEmail.equals(oldEmail)) {
                        UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                                newEmail,
                                null,
                                authentication.getAuthorities()
                        );
                        SecurityContextHolder.getContext().setAuthentication(newAuth);
                    }

                    // Mensaje éxito
                    redirectAttributes.addFlashAttribute("successMessage", "¡Tu perfil ha sido actualizado con éxito!");
                }

            } catch (IllegalStateException e) {
                // Captura excepción y extrae mensaje correo en uso
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

                // Redirigir de vuelta al formulario de perfil, para que muestre el error
                return "redirect:/perfil";
            }

        } else {
            // usuario no encontrado en sesión
            redirectAttributes.addFlashAttribute("errorMessage", "Error de sesión: Usuario no encontrado.");
            return "redirect:/login";
        }
        return "redirect:/perfil";
    }

    // PerfilController
    @PostMapping("/solicitar-cambio-password")
    public String solicitarCambioPassword(Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            // Llamar al servicio "usuarioService.createPasswordResetTokenForUser()" para que inicie toda la lógica,
            //  pasándo el correo del usuario al servicio. Delega la responsabilidad al servicio "UsuarioServiceImpl"
            usuarioService.createPasswordResetTokenForUser(authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Se ha enviado un correo con las instrucciones para cambiar la contraseña.");
        } catch (Exception e) {
            // Manejo de errores
            redirectAttributes.addFlashAttribute("errorMessage",
                    "No se pudo procesar la solicitud. Inténtalo de nuevo.");
        }
        return "redirect:/perfil";
    }
}