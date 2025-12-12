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
@RequestMapping("/admin/correos")
@PreAuthorize("hasRole('Administrador')") // Asegura que solo Admins accedan
public class CorreoMasivoController {

    @Autowired
    private UsuarioService usuarioService; // inyecta el servicio que obtiene usuarios y envia correos masivos

    // formulario para redactar el correo
    @GetMapping("/nuevo")
    public String showBulkEmailForm(Model model) {
        // agrega al modelo los roles permitidos para envío masivo
        model.addAttribute("roles", new RolNombre[]{RolNombre.Familiar, RolNombre.Cuidador});
        return "correo/admin-formulario-correo-masivo";
    }

    // Procesa el envío del formulario
    @PostMapping("/enviar")
    public String sendBulkEmail(@RequestParam("targetRole") String targetRole,
                                @RequestParam("subject") String subject,
                                @RequestParam("body") String body,
                                RedirectAttributes redirectAttributes) {

        // variable que almacena el rol seleccionado por el administrador
        RolNombre roleToSend = null;
        try {
            // valida el rol recibido desde el formulario y define el rol real del sistema
            if ("Familiar".equals(targetRole)) {
                roleToSend = RolNombre.Familiar;
            } else if ("Cuidador".equals(targetRole)) {
                roleToSend = RolNombre.Cuidador;
            } else if ("TODOS".equals(targetRole)) {
                // valor null indica que el envío incluye a todos los roles permitidos
                roleToSend = null;
            } else {
                throw new IllegalArgumentException("Rol de destino inválido.");
            }

            // valida que el asunto y el cuerpo no estén vacíos
            if (subject.isBlank() || body.isBlank()) {
                throw new IllegalArgumentException("El asunto y el cuerpo del mensaje no pueden estar vacíos.");
            }

            // llama el servicio que obtiene los correos y ejecuta el envío masivo
            usuarioService.sendCustomBulkEmailToRole(roleToSend, subject, body);
            // guarda un mensaje de éxito temporal para mostrar en la vista
            redirectAttributes.addFlashAttribute("successMessage", "Correo masivo enviado correctamente a: " + targetRole);

        } catch (IllegalArgumentException e) {
            // captura errores
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
            return "redirect:/admin/correos/nuevo";
        } catch (Exception e) {
            System.err.println("Error al enviar correo masivo: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Ocurrió un error inesperado al enviar los correos.");
            return "redirect:/admin/correos/nuevo";
        }
        // redirección al formulario
        return "redirect:/admin/correos/nuevo";
    }
}