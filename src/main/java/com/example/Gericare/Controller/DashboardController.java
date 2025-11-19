package com.example.Gericare.Controller;

import com.example.Gericare.DTO.EstadisticaActividadDTO;
import com.example.Gericare.DTO.UsuarioDTO;
import com.example.Gericare.Repository.PacienteAsignadoRepository;
import com.example.Gericare.Service.ActividadService;
import com.example.Gericare.Service.EstadisticaService;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final UsuarioService usuarioService;
    private final ActividadService actividadService;
    private final EstadisticaService estadisticaService;

    @Autowired
    private PacienteAsignadoRepository pacienteAsignadoRepository;

    public DashboardController(UsuarioService usuarioService, ActividadService actividadService, EstadisticaService estadisticaService) {
        this.usuarioService = usuarioService;
        this.actividadService = actividadService;
        this.estadisticaService = estadisticaService;
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

            // Lógica para el color del header según el rol
            String headerClass = "bg-dark"; // Default Admin
            if (userRole.equals("ROLE_Cuidador")) {
                headerClass = "bg-role-cuidador";
            } else if (userRole.equals("ROLE_Familiar")) {
                headerClass = "bg-role-familiar";
            }
            model.addAttribute("headerClass", headerClass);

            // Alerta cambio contraseña
            boolean mostrarAlerta = false;
            if (userRole.equals("ROLE_Cuidador") || userRole.equals("ROLE_Familiar")) {
                Optional<UsuarioDTO> usuarioOpt = usuarioService.findByEmail(userEmail);
                if (usuarioOpt.isPresent() && usuarioOpt.get().isNecesitaCambioContrasena()) {
                    mostrarAlerta = true;
                }
            }
            model.addAttribute("mostrarAlertaCambioContrasena", mostrarAlerta);

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

                // NUEVO: Datos para el gráfico de torta (Admin)
                List<EstadisticaActividadDTO> statsActividades = estadisticaService.obtenerEstadisticasActividadesCompletadas();
                model.addAttribute("statsActividades", statsActividades);

                // Preparar datos para Chart.js (Strings separados por coma)
                String labels = statsActividades.stream().map(s -> "'" + s.getNombreCompleto() + "'").collect(Collectors.joining(","));
                String data = statsActividades.stream().map(s -> s.getActividadesCompletadas().toString()).collect(Collectors.joining(","));
                model.addAttribute("chartLabels", "[" + labels + "]");
                model.addAttribute("chartData", "[" + data + "]");

            } else if (userRole.equals("ROLE_Cuidador")) {
                model.addAttribute("pacientesAsignados", usuarioService.findPacientesByCuidadorEmail(userEmail));

            } else if (userRole.equals("ROLE_Familiar")) {
                model.addAttribute("pacientesAsignados", usuarioService.findPacientesByFamiliarEmail(userEmail));
            }
        }
        return "dashboard";
    }
}