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
        String result = usuarioService.validatePasswordResetToken(token);
        if (result != null) {
            redirectAttributes.addFlashAttribute("errorMessage", "El enlace para cambiar la contraseña es inválido o ha expirado.");
            return "redirect:/login";
        }
        model.addAttribute("token", token);
        return "reset-password-form";
    }

    @PostMapping("/reset-password")
    public String handlePasswordReset(@RequestParam("token") String token,
                                      @RequestParam("password") String password,
                                      @RequestParam("confirmPassword") String confirmPassword,
                                      RedirectAttributes redirectAttributes) {

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

        usuarioService.changeUserPassword(token, password);
        redirectAttributes.addFlashAttribute("successMessage", "Tu contraseña ha sido cambiada exitosamente. Por favor, inicia sesión.");
        return "redirect:/login";
    }
}