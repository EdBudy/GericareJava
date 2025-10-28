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

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model, RedirectAttributes redirectAttributes) {
        // Validar el token recibido desde la URL
        // El método "validatePasswordResetToken" en UsuarioServiceImpl busca el token en la bd y verifica que la fecha de expiración no haya pasado
        String result = usuarioService.validatePasswordResetToken(token);

        if (result != null) { // Si el resultado no es nulo, hubo un error (token inválido o expirado)
            redirectAttributes.addFlashAttribute("errorMessage", "El enlace para cambiar la contraseña es inválido o ha expirado.");
            return "redirect:/login";
        }

        // Si es válido, muestra el formulario para cambiar la contraseña
        model.addAttribute("token", token);
        return "reset-password-form";
    }

    // En "reset-password-form.html" el usuario ingresa y cambia su contraseña
    @PostMapping("/reset-password")
    public String handlePasswordReset(@RequestParam("token") String token,
                                      @RequestParam("password") String password,
                                      @RequestParam("confirmPassword") String confirmPassword,
                                      RedirectAttributes redirectAttributes) {

        // Validar el token de nuevo por seguridad y que las contraseñas coincidan
        String result = usuarioService.validatePasswordResetToken(token);
        if (result != null) {
            redirectAttributes.addFlashAttribute("errorMessage", "El enlace para cambiar la contraseña es inválido o ha expirado.");
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
            // Llamar al servicio para hacer el cambio de contraseña
            usuarioService.changeUserPassword(token, password);
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/reset-password?token=" + token;
        }

        redirectAttributes.addFlashAttribute("successMessage", "Tu contraseña ha sido cambiada exitosamente. Por favor, inicia sesión.");
        return "redirect:/login";
    }
}