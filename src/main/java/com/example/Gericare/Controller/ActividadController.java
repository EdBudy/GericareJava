package com.example.Gericare.Controller;

import com.example.Gericare.DTO.ActividadDTO;
import com.example.Gericare.Service.ActividadService;
import com.example.Gericare.Service.PacienteService;
import com.example.Gericare.Service.UsuarioService;
import com.example.Gericare.Enums.EstadoActividad;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/actividades")
public class ActividadController {

    @Autowired
    private ActividadService actividadService;

    @Autowired
    private PacienteService pacienteService;

    @Autowired
    private UsuarioService usuarioService;

    // Vistas admin
    @GetMapping
    public String listarActividades(Model model,
                                    @RequestParam(required = false) String nombrePaciente,
                                    @RequestParam(required = false) String tipoActividad,
                                    @RequestParam(required = false) EstadoActividad estado) {
        model.addAttribute("actividades", actividadService.listarActividades(nombrePaciente, tipoActividad, estado));
        return "actividad/admin-gestion-actividades";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevaActividad(Model model) {
        if (!model.containsAttribute("actividad")) {
            model.addAttribute("actividad", new ActividadDTO());
        }
        model.addAttribute("pacientes", pacienteService.listarPacientesFiltrados(null, null));
        return "actividad/admin-formulario-actividad";
    }

    @PostMapping("/crear")
    public String crearActividad(@Valid @ModelAttribute("actividad") ActividadDTO actividadDTO,
                                 BindingResult bindingResult, Authentication authentication, Model model,
                                 RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("pacientes", pacienteService.listarPacientesFiltrados(null, null));
            return "actividad/admin-formulario-actividad";
        }

        String adminEmail = authentication.getName();
        Long adminId = usuarioService.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin no encontrado")).getIdUsuario();
        actividadDTO.setIdAdmin(adminId);

        actividadService.crearActividad(actividadDTO);
        redirectAttributes.addFlashAttribute("successMessage", "¡Actividad creada con éxito!");
        return "redirect:/actividades";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditarActividad(@PathVariable Long id, Model model) {
        actividadService.obtenerActividadPorId(id).ifPresent(actividad -> {
            model.addAttribute("actividad", actividad);
            model.addAttribute("pacientes", pacienteService.listarPacientesFiltrados(null, null));
        });
        return "actividad/admin-formulario-actividad";
    }

    @PostMapping("/editar/{id}")
    public String actualizarActividad(@PathVariable Long id, @Valid @ModelAttribute("actividad") ActividadDTO actividadDTO,
                                      BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("pacientes", pacienteService.listarPacientesFiltrados(null, null));
            return "actividad/admin-formulario-actividad";
        }

        actividadService.actualizarActividad(id, actividadDTO);
        redirectAttributes.addFlashAttribute("successMessage", "¡Actividad actualizada con éxito!");
        return "redirect:/actividades";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarActividad(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        actividadService.eliminarActividad(id);
        redirectAttributes.addFlashAttribute("successMessage", "¡Actividad eliminada con éxito!");
        return "redirect:/actividades";
    }

    // Acción cuidador
    @PostMapping("/completar/{id}")
    public String completarActividad(@PathVariable("id") Long actividadId, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            Long cuidadorId = usuarioService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Cuidador no encontrado"))
                    .getIdUsuario();
            actividadService.marcarComoCompletada(actividadId, cuidadorId);
            redirectAttributes.addFlashAttribute("successMessage", "Actividad completada con éxito.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al completar la actividad: " + e.getMessage());
        }
        return "redirect:/actividades/actividades-pacientes";
    }

    // Vista cuidador
    @GetMapping("/actividades-pacientes")
    public String mostrarActividadesCuidador(Authentication authentication, Model model) {
        String userEmail = authentication.getName();
        Long cuidadorId = usuarioService.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Cuidador no encontrado"))
                .getIdUsuario();
        model.addAttribute("actividades", actividadService.listarActividadesPorCuidador(cuidadorId));
        return "actividad/cuidador-actividades";
    }
}
