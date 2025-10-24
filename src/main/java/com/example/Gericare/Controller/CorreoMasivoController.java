package com.example.Gericare.Controller;

import com.example.Gericare.Enums.RolNombre;
import com.example.Gericare.Service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/correos") // Ruta base para este controlador
@PreAuthorize("hasRole('Administrador')") // Asegura que solo Admins accedan
public class CorreoMasivoController {

    @Autowired
    private UsuarioService usuarioService;

    // Muestra el formulario para redactar el correo
    @GetMapping("/nuevo")
    public String showBulkEmailForm(Model model) {
        // Pasamos los roles posibles al modelo para el <select>
        // Usamos null o un valor especial como "TODOS" para la opción de enviar a ambos
        model.addAttribute("roles", new RolNombre[]{RolNombre.Familiar, RolNombre.Cuidador});
        return "formulario-correo-masivo"; // Nombre del archivo HTML que crearemos
    }

    // Procesa el envío del formulario
    @PostMapping("/enviar")
    public String sendBulkEmail(@RequestParam("targetRole") String targetRole, // "Familiar", "Cuidador" o "TODOS"
                                @RequestParam("subject") String subject,
                                @RequestParam("body") String body,
                                RedirectAttributes redirectAttributes) {

        RolNombre roleToSend = null;
        try {
            if ("Familiar".equals(targetRole)) {
                roleToSend = RolNombre.Familiar;
            } else if ("Cuidador".equals(targetRole)) {
                roleToSend = RolNombre.Cuidador;
            } else if ("TODOS".equals(targetRole)) {
                roleToSend = null; // Usamos null para indicar "Todos" (Familiar y Cuidador)
            } else {
                throw new IllegalArgumentException("Rol de destino inválido.");
            }

            if (subject.isBlank() || body.isBlank()) {
                throw new IllegalArgumentException("El asunto y el cuerpo del mensaje no pueden estar vacíos.");
            }

            usuarioService.sendCustomBulkEmailToRole(roleToSend, subject, body);
            redirectAttributes.addFlashAttribute("successMessage", "Correo masivo enviado correctamente a: " + targetRole);

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
            // Redirigir de vuelta al formulario para corregir
            return "redirect:/admin/correos/nuevo";
        } catch (Exception e) {
            System.err.println("Error al enviar correo masivo: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Ocurrió un error inesperado al enviar los correos.");
            // Redirigir de vuelta al formulario
            return "redirect:/admin/correos/nuevo";
        }

        return "redirect:/admin/correos/nuevo"; // Redirige de vuelta al formulario después de enviar
        // O podrías redirigir al dashboard: return "redirect:/dashboard";
    }
}