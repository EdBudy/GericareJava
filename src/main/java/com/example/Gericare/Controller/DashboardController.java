package com.example.Gericare.Controller;

import com.example.Gericare.DTO.UsuarioDTO;
import com.example.Gericare.Repository.PacienteAsignadoRepository;
import com.example.Gericare.Service.ActividadService;
import com.example.Gericare.Service.UsuarioService;
import com.example.Gericare.entity.PacienteAsignado;
import com.example.Gericare.enums.RolNombre;
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
    private final ActividadService actividadService;

    @Autowired
    private PacienteAsignadoRepository pacienteAsignadoRepository;

    public DashboardController(UsuarioService usuarioService, ActividadService actividadService) {
        this.usuarioService = usuarioService;
        this.actividadService = actividadService;
    }

    @GetMapping
    public String mostrarDashboard(Authentication authentication, Model model,
                                   @RequestParam(required = false) String nombre,
                                   @RequestParam(required = false) String documento,
                                   @RequestParam(required = false) RolNombre rol) {

        // Obtener el rol del usuario que inició sesión
        String userRole = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);

        if (userRole != null) {
            String userEmail = authentication.getName();

            // Comparar el rol y ejecutar una lógica diferente para cada uno
            if (userRole.equals("ROLE_Administrador")) {
                // Si es admin busca todos los usuarios y los añade al modelo
                List<UsuarioDTO> usuarios = usuarioService.findUsuariosByCriteria(nombre, documento, rol, userEmail);
                model.addAttribute("usuarios", usuarios);

                // Se crea un mapa para almacenar los mensajes de advertencia
                Map<Long, String> familiarAssignmentsText = new HashMap<>();
                for (UsuarioDTO usuario : usuarios) {
                    // Se verifica si el usuario es un Familiar
                    if (usuario.getRol().getRolNombre() == RolNombre.Familiar) {
                        // Se buscan las asignaciones de ese familiar
                        List<PacienteAsignado> asignaciones = pacienteAsignadoRepository.findByFamiliar_idUsuario(usuario.getIdUsuario());
                        if (!asignaciones.isEmpty()) {
                            // Si tiene asignaciones, se crea el texto de advertencia
                            String pacientesAsignados = asignaciones.stream()
                                    .map(pa -> pa.getPaciente().getNombre() + " " + pa.getPaciente().getApellido())
                                    .collect(Collectors.joining(", "));
                            familiarAssignmentsText.put(usuario.getIdUsuario(), "Este familiar está asignado a: " + pacientesAsignados + ". Si lo eliminas, se desvinculará. ¿Deseas continuar?");
                        }
                    }
                }
                // Se añade el mapa al modelo para que la vista lo pueda usar.
                model.addAttribute("familiarAssignmentsText", familiarAssignmentsText);

                model.addAttribute("roles", RolNombre.values());

            } else if (userRole.equals("ROLE_Cuidador")) {
                // Cuidador, busca solo los pacientes asignados a ese cuidador
                model.addAttribute("pacientesAsignados", usuarioService.findPacientesByCuidadorEmail(userEmail));

            } else if (userRole.equals("ROLE_Familiar")) {
                // Familiar busca solo los pacientes asociados a ese familiar
                model.addAttribute("pacientesAsignados", usuarioService.findPacientesByFamiliarEmail(userEmail));
            }
        }
        // Envía los datos cargados a la misma vista "dashboard.html"
        return "dashboard";
    }
}