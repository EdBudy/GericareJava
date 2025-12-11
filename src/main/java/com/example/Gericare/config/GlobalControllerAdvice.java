package com.example.Gericare.config;

import com.example.Gericare.DTO.UsuarioDTO;
import com.example.Gericare.Service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private UsuarioService usuarioService;

    @ModelAttribute
    public void addGlobalAttributes(Authentication authentication, Model model) {
        // Valores por defecto
        String headerClass = "bg-dark";

        if (authentication != null && authentication.isAuthenticated()) {
            String userRole = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("");

            String userEmail = authentication.getName();

            // 1. Lógica de Colores Global por Rol
            if ("ROLE_Cuidador".equals(userRole)) {
                headerClass = "bg-role-cuidador";
            } else if ("ROLE_Familiar".equals(userRole)) {
                headerClass = "bg-role-familiar";
            }

            // 2. Alerta de cambio de contraseña (Global)
            if ("ROLE_Cuidador".equals(userRole) || "ROLE_Familiar".equals(userRole)) {
                Optional<UsuarioDTO> usuarioOpt = usuarioService.findByEmail(userEmail);
                if (usuarioOpt.isPresent() && usuarioOpt.get().isNecesitaCambioContrasena()) {
                    model.addAttribute("mostrarAlertaCambioContrasena", true);
                }
            }
        }

        // Inyectamos la clase CSS al modelo globalmente
        model.addAttribute("headerClass", headerClass);
    }
}