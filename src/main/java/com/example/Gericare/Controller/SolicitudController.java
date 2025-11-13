package com.example.Gericare.Controller;

import com.example.Gericare.DTO.PacienteDTO;
import com.example.Gericare.DTO.SolicitudDTO;
import com.example.Gericare.Enums.RolNombre;
import com.example.Gericare.Service.PacienteService;
import com.example.Gericare.Service.SolicitudService;
import com.example.Gericare.Service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/solicitudes")
public class SolicitudController {

    @Autowired
    private SolicitudService solicitudService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PacienteService pacienteService; // Para obtener lista de pacientes asignados al familiar

    // Vistas Familiar

    @GetMapping("/mis-solicitudes")
    @PreAuthorize("hasRole('Familiar')")
    public String listarMisSolicitudes(Authentication authentication, Model model) {
        Long familiarId = usuarioService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Familiar no encontrado"))
                .getIdUsuario();
        model.addAttribute("solicitudes", solicitudService.listarSolicitudesActivasPorFamiliar(familiarId));
        return "solicitud/familiar-mis-solicitudes";
    }

    @GetMapping("/nueva")
    @PreAuthorize("hasRole('Familiar')")
    public String mostrarFormularioNuevaSolicitud(Authentication authentication, Model model) {
        Long familiarId = usuarioService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Familiar no encontrado"))
                .getIdUsuario();

        // Obtener solo los pacientes asociados a este familiar
        List<PacienteDTO> pacientesAsociados = usuarioService.findPacientesByFamiliarEmail(authentication.getName())
                .stream()
                .map(paDTO -> paDTO.getPaciente()) // Extraer solo el PacienteDTO de la asignación
                .collect(Collectors.toList());

        if (pacientesAsociados.isEmpty()) {
            model.addAttribute("errorMessage", "No tiene pacientes asignados para crear solicitudes.");
            return "solicitud/familiar-mis-solicitudes";
        }


        if (!model.containsAttribute("solicitud")) {
            model.addAttribute("solicitud", new SolicitudDTO());
        }
        model.addAttribute("pacientesAsociados", pacientesAsociados);
        // Pasar los tipos de solicitud al modelo para el select
        model.addAttribute("tiposSolicitud", com.example.Gericare.Enums.TipoSolicitud.values());
        return "solicitud/familiar-formulario-solicitud";
    }

    @PostMapping("/crear")
    @PreAuthorize("hasRole('Familiar')")
    public String crearSolicitud(@Valid @ModelAttribute("solicitud") SolicitudDTO solicitudDTO,
                                 BindingResult bindingResult, Authentication authentication,
                                 Model model, RedirectAttributes redirectAttributes) {

        Long familiarId = usuarioService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Familiar no encontrado"))
                .getIdUsuario();

        // Validar que el paciente seleccionado pertenece al familiar
        boolean pacienteValido = usuarioService.findPacientesByFamiliarEmail(authentication.getName())
                .stream()
                .anyMatch(pa -> pa.getPaciente().getIdPaciente().equals(solicitudDTO.getPacienteId()));

        if (!pacienteValido) {
            // Manejar error el paciente no le pertenece
            bindingResult.rejectValue("pacienteId", "error.solicitud", "Paciente inválido seleccionado.");
        }


        if (bindingResult.hasErrors()) {
            // Volver a cargar la lista de pacientes del familiar
            List<PacienteDTO> pacientesAsociados = usuarioService.findPacientesByFamiliarEmail(authentication.getName())
                    .stream()
                    .map(paDTO -> paDTO.getPaciente())
                    .collect(Collectors.toList());
            model.addAttribute("pacientesAsociados", pacientesAsociados);
            model.addAttribute("tiposSolicitud", com.example.Gericare.Enums.TipoSolicitud.values());
            return "solicitud/familiar-formulario-solicitud";
        }

        try {
            solicitudService.crearSolicitud(solicitudDTO, familiarId);
            redirectAttributes.addFlashAttribute("successMessage", "¡Solicitud creada con éxito! Se encuentra pendiente de revisión.");
            return "redirect:/solicitudes/mis-solicitudes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al crear la solicitud: " + e.getMessage());
            // Volver a cargar datos necesarios para el formulario
            redirectAttributes.addFlashAttribute("solicitud", solicitudDTO); // Devolver datos ingresados
            // No se puede recargar pacientes fácilmente con redirect, redirigir a GET /nueva
            return "redirect:/solicitudes/nueva";
        }
    }

    @PostMapping("/eliminar/{id}")
    @PreAuthorize("hasAnyRole('Familiar', 'Administrador')")
    public String eliminarSolicitud(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        Long usuarioId = usuarioService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"))
                .getIdUsuario();
        // Obtener el rol del Authentication object
        String rolUsuario = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", "")) // Quitar prefijo ROLE_
                .findFirst()
                .orElse(null); // Rol como String "Administrador" o "Familiar"

        try {
            solicitudService.eliminarSolicitudLogico(id, usuarioId, rolUsuario);
            redirectAttributes.addFlashAttribute("successMessage", "¡Solicitud eliminada con éxito!");
        } catch (IllegalStateException | AccessDeniedException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar la solicitud.");
        }

        // Redirigir según rol
        if (rolUsuario.equals(RolNombre.Administrador.name())) {
            return "redirect:/solicitudes/admin";
        } else {
            return "redirect:/solicitudes/mis-solicitudes";
        }
    }


    // Vistas/Acciones Administrador

    @GetMapping("/admin")
    @PreAuthorize("hasRole('Administrador')")
    public String listarTodasSolicitudesAdmin(Model model) {
        model.addAttribute("solicitudes", solicitudService.listarTodasSolicitudesActivas());
        return "solicitud/admin-gestion-solicitudes";
    }

    @PostMapping("/aprobar/{id}")
    @PreAuthorize("hasRole('Administrador')")
    public String aprobarSolicitud(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        Long adminId = usuarioService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Admin no encontrado"))
                .getIdUsuario();
        try {
            solicitudService.aprobarSolicitud(id, adminId);
            redirectAttributes.addFlashAttribute("successMessage", "Solicitud #" + id + " aprobada.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al aprobar la solicitud.");
        }
        return "redirect:/solicitudes/admin";
    }

    @PostMapping("/rechazar/{id}")
    @PreAuthorize("hasRole('Administrador')")
    public String rechazarSolicitud(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        Long adminId = usuarioService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Admin no encontrado"))
                .getIdUsuario();
        try {
            solicitudService.rechazarSolicitud(id, adminId);
            redirectAttributes.addFlashAttribute("successMessage", "Solicitud #" + id + " rechazada.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al rechazar la solicitud.");
        }
        return "redirect:/solicitudes/admin";
    }
}