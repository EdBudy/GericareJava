package com.example.Gericare.Controller;

import com.example.Gericare.Service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PasswordResetController {

    @Autowired
    private UsuarioService usuarioService;

    // 1. Mostrar formulario para pedir el correo
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "correo/recuperacion-clave";
    }

    // 2. Procesar el correo y enviar el email con el token
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.createPasswordResetTokenForUser(email);
            redirectAttributes.addFlashAttribute("successMessage", "Si el correo existe, recibirás un enlace para restablecer tu clave.");
        } catch (Exception e) {
            // Por seguridad, mostramos el mismo mensaje aunque el correo no exista
            redirectAttributes.addFlashAttribute("successMessage", "Si el correo existe, recibirás un enlace para restablecer tu clave.");
        }
        return "redirect:/forgot-password";
    }

    // 3. Mostrar el formulario para escribir la NUEVA clave (usa el token de la URL)
    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model, RedirectAttributes redirectAttributes) {
        String result = usuarioService.validatePasswordResetToken(token);

        if (result != null) {
            redirectAttributes.addFlashAttribute("errorMessage", "El enlace es inválido o ha expirado.");
            return "redirect:/login";
        }

        // IMPORTANTE: Pasamos el token al HTML para que no se pierda
        model.addAttribute("token", token);
        return "correo/reset-password-form";
    }

    // 4. Procesar el cambio de clave final
    @PostMapping("/reset-password")
    public String handlePasswordReset(@RequestParam("token") String token,
                                      @RequestParam("password") String password,
                                      @RequestParam("confirmPassword") String confirmPassword,
                                      RedirectAttributes redirectAttributes) {

        // Validar token de nuevo
        String result = usuarioService.validatePasswordResetToken(token);
        if (result != null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Sesión de recuperación expirada.");
            return "redirect:/login";
        }

        if (password.length() < 8) {
            redirectAttributes.addFlashAttribute("error", "La contraseña debe tener al menos 8 caracteres.");
            return "redirect:/reset-password?token=" + token;
        }

        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden.");
            return "redirect:/reset-password?token=" + token;
        }

        try {
            usuarioService.changeUserPassword(token, password);
            redirectAttributes.addFlashAttribute("successMessage", "Contraseña cambiada con éxito. Ya puedes iniciar sesión.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/reset-password?token=" + token;
        }
    }
}
