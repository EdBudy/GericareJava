package com.example.Gericare.Controller;

import com.example.Gericare.Service.UsuarioService;
import com.example.Gericare.enums.RolNombre;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final UsuarioService usuarioService;

    public DashboardController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String mostrarDashboard(Authentication authentication, Model model,
                                   @RequestParam(required = false) String nombre,
                                   @RequestParam(required = false) String documento,
                                   @RequestParam(required = false) RolNombre rol) {

        String userRole = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);

        // ¡VERSIÓN CORREGIDA Y SIMPLIFICADA!
        if (userRole != null) {
            if (userRole.equals("ROLE_Administrador")) {
                // Obtener el email del usuario logueado
                String adminEmail = authentication.getName();
                // Pasar email al servicio para que sea excluido de la lista
                model.addAttribute("usuarios", usuarioService.findUsuariosByCriteria(nombre, documento, rol, adminEmail));
                model.addAttribute("roles", RolNombre.values());
            } else if (userRole.equals("ROLE_Cuidador")) {
                model.addAttribute("pacientesAsignados", usuarioService.findPacientesByCuidadorEmail(authentication.getName()));
            } else if (userRole.equals("ROLE_Familiar")) {
                usuarioService.findPacientesByFamiliarEmail(authentication.getName())
                        .ifPresent(asignacion -> model.addAttribute("pacienteAsignado", asignacion));
            }
        }

        // Este return se ejecuta siempre, asegurando que el método siempre devuelva un String.
        return "dashboard";
    }
}