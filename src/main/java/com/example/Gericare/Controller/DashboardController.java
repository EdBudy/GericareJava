package com.example.Gericare.Controller;

import com.example.Gericare.Service.ActividadService;
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
    private final ActividadService actividadService;

    public DashboardController(UsuarioService usuarioService, ActividadService actividadService) {
        this.usuarioService = usuarioService;
        this.actividadService = actividadService;
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

        if (userRole != null) {
            String userEmail = authentication.getName();

            if (userRole.equals("ROLE_Administrador")) {
                model.addAttribute("usuarios", usuarioService.findUsuariosByCriteria(nombre, documento, rol, userEmail));
                model.addAttribute("roles", RolNombre.values());

            } else if (userRole.equals("ROLE_Cuidador")) {
                // AHORA SOLO CARGA PACIENTES
                model.addAttribute("pacientesAsignados", usuarioService.findPacientesByCuidadorEmail(userEmail));

            } else if (userRole.equals("ROLE_Familiar")) {
                model.addAttribute("pacienteAsignado", usuarioService.findPacientesByFamiliarEmail(userEmail));
            }
        }
        return "dashboard";
    }
}