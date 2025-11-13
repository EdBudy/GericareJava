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
    public String actualizarPerfil(Authentication authentication, @ModelAttribute("usuario") UsuarioDTO usuarioDTO) {

        String oldEmail = authentication.getName();

        // Obtener el ID del usuario actual (basado en el email de la sesión)
        Optional<Long> usuarioIdOpt = usuarioService.findByEmail(oldEmail).map(UsuarioDTO::getIdUsuario);

        if (usuarioIdOpt.isPresent()) {

            // Realizar la actualización y capturar el resultado del servicio
            Optional<UsuarioDTO> usuarioActualizadoOpt = usuarioService.actualizarUsuario(usuarioIdOpt.get(), usuarioDTO);

            if (usuarioActualizadoOpt.isPresent()) {
                String newEmail = usuarioActualizadoOpt.get().getCorreoElectronico();

                // Comprobar si correo electrónico cambió
                if (newEmail != null && !newEmail.equals(oldEmail)) {

                    // Si si, crea nuevo token de autenticación
                    UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                            newEmail,                     // El nuevo principal (username)
                            null,                         // Las credenciales (password, no las tiene y no son necesarias aquí)
                            authentication.getAuthorities()
                    );

                    // Establecer la nueva autenticación en el contexto de seguridad ("refresca" la sesión del usuario con el nuevo email)
                    SecurityContextHolder.getContext().setAuthentication(newAuth);
                }
            }

        } else {
            return "redirect:/login?error=userNotFound";
        }
        return "redirect:/dashboard?perfilActualizado=true";
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