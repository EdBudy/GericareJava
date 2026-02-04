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
            // Mensaje de éxito siempre (por seguridad)
            redirectAttributes.addFlashAttribute("successMessage", "Si el correo está registrado, recibirás un enlace.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("successMessage", "Si el correo está registrado, recibirás un enlace.");
        }
        return "redirect:/forgot-password";
    }

    // 3. Mostrar el formulario para escribir la NUEVA clave
    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model, RedirectAttributes redirectAttributes) {
        // Validar si el token sirve
        String result = usuarioService.validatePasswordResetToken(token);

        if (result != null) {
            redirectAttributes.addFlashAttribute("errorMessage", "El enlace es inválido o ha expirado.");
            return "redirect:/login";
        }

        // Pasamos el token al HTML para que el input hidden lo coja
        model.addAttribute("token", token);
        
        // ESTA RUTA DEBE COINCIDIR CON DONDE GUARDAS TU HTML QUE ME PASASTE
        // Asumo que está en src/main/resources/templates/correo/reset-password-form.html
        return "correo/reset-password-form";
    }

    // 4. Procesar el cambio de clave final
    @PostMapping("/reset-password")
    public String handlePasswordReset(@RequestParam("token") String token,
                                      @RequestParam("password") String password,
                                      @RequestParam("confirmPassword") String confirmPassword,
                                      RedirectAttributes redirectAttributes) {

        String result = usuarioService.validatePasswordResetToken(token);
        if (result != null) {
            redirectAttributes.addFlashAttribute("errorMessage", "El enlace ya caducó.");
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
            redirectAttributes.addFlashAttribute("successMessage", "Contraseña cambiada exitosamente. Inicia sesión.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/reset-password?token=" + token;
        }
    }
}
