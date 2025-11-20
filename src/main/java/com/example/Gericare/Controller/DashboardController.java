package com.example.Gericare.Controller;

import com.example.Gericare.DTO.UsuarioDTO;
import com.example.Gericare.Repository.PacienteAsignadoRepository;
import com.example.Gericare.Service.ActividadService;
import com.example.Gericare.Service.UsuarioService;
import com.example.Gericare.Entity.PacienteAsignado;
import com.example.Gericare.Enums.RolNombre;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final UsuarioService usuarioService;

    @Autowired
    private PacienteAsignadoRepository pacienteAsignadoRepository;

    public DashboardController(UsuarioService usuarioService, ActividadService actividadService) {
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

        if (userRole != null) {
            String userEmail = authentication.getName();

            if (userRole.equals("ROLE_Administrador")) {
                List<UsuarioDTO> usuarios = usuarioService.findUsuariosByCriteria(nombre, documento, rol);
                model.addAttribute("usuarios", usuarios);

                Map<Long, String> familiarAssignmentsText = new HashMap<>();
                for (UsuarioDTO usuario : usuarios) {
                    if (usuario.getRol().getRolNombre() == RolNombre.Familiar) {
                        List<PacienteAsignado> asignaciones = pacienteAsignadoRepository.findByFamiliar_idUsuario(usuario.getIdUsuario());
                        if (!asignaciones.isEmpty()) {
                            String pacientesAsignados = asignaciones.stream()
                                    .map(pa -> pa.getPaciente().getNombre() + " " + pa.getPaciente().getApellido())
                                    .collect(Collectors.joining(", "));
                            familiarAssignmentsText.put(usuario.getIdUsuario(), "Este familiar está asignado a: " + pacientesAsignados + ". Si lo eliminas, se desvinculará. ¿Deseas continuar?");
                        }
                    }
                }
                model.addAttribute("familiarAssignmentsText", familiarAssignmentsText);
                model.addAttribute("roles", RolNombre.values());

            } else if (userRole.equals("ROLE_Cuidador")) {
                model.addAttribute("pacientesAsignados", usuarioService.findPacientesByCuidadorEmail(userEmail));

            } else if (userRole.equals("ROLE_Familiar")) {
                model.addAttribute("pacientesAsignados", usuarioService.findPacientesByFamiliarEmail(userEmail));
            }
        }
        return "dashboard";
    }
}