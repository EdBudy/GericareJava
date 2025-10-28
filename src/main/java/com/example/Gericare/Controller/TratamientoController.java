package com.example.Gericare.Controller;

import com.example.Gericare.DTO.PacienteAsignadoDTO;
import com.example.Gericare.DTO.TratamientoDTO;
import com.example.Gericare.Entity.Cuidador;
import com.example.Gericare.Enums.EstadoAsignacion;
import com.example.Gericare.Enums.RolNombre;
import com.example.Gericare.Repository.PacienteAsignadoRepository;
import com.example.Gericare.Service.PacienteService;
import com.example.Gericare.Service.TratamientoService;
import com.example.Gericare.Service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/tratamientos")
public class TratamientoController {

    @Autowired
    private TratamientoService tratamientoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PacienteService pacienteService; // obtener lista de pacientes (Admin)

    @Autowired
    private PacienteAsignadoRepository pacienteAsignadoRepository; // buscar cuidador asignado

    // Vista/Acciones Administrador

    @GetMapping("/admin")
    @PreAuthorize("hasRole('Administrador')")
    public String listarTodosTratamientosAdmin(Model model) {
        model.addAttribute("tratamientos", tratamientoService.listarTodosTratamientosActivos());
        return "gestion-tratamientos-admin";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasRole('Administrador')")
    public String mostrarFormularioNuevoTratamiento(Model model) {
        if (!model.containsAttribute("tratamiento")) {
            model.addAttribute("tratamiento", new TratamientoDTO());
        }
        // Obtener todos los pacientes activos para el select inicial
        model.addAttribute("pacientes", pacienteService.listarPacientesFiltrados(null, null));
        return "formulario-tratamiento";
    }

    @PostMapping("/crear")
    @PreAuthorize("hasRole('Administrador')")
    public String crearTratamiento(@Valid @ModelAttribute("tratamiento") TratamientoDTO tratamientoDTO,
                                   BindingResult bindingResult, Authentication authentication,
                                   Model model, RedirectAttributes redirectAttributes) {

        Long adminId = usuarioService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Admin no encontrado"))
                .getIdUsuario();

        if (bindingResult.hasErrors()) {
            model.addAttribute("pacientes", pacienteService.listarPacientesFiltrados(null, null));
            return "formulario-tratamiento";
        }

        try {
            tratamientoService.crearTratamiento(tratamientoDTO, adminId);
            redirectAttributes.addFlashAttribute("successMessage", "¡Tratamiento creado con éxito!");
            return "redirect:/tratamientos/admin";
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("tratamiento", tratamientoDTO);
            return "redirect:/tratamientos/nuevo";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al crear el tratamiento: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tratamiento", tratamientoDTO);
            return "redirect:/tratamientos/nuevo";
        }
    }

    @GetMapping("/editar/{id}")
    @PreAuthorize("hasAnyRole('Administrador', 'Cuidador')")
    public String mostrarFormularioEditarTratamiento(@PathVariable Long id, Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        Optional<TratamientoDTO> tratamientoOpt = tratamientoService.obtenerTratamientoPorId(id);

        if (tratamientoOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tratamiento no encontrado.");
            // Redirigir según rol
            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_Administrador"))) {
                return "redirect:/tratamientos/admin";
            } else {
                return "redirect:/tratamientos/mis-tratamientos";
            }
        }

        TratamientoDTO tratamiento = tratamientoOpt.get();
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_Administrador"));

        // Si es cuidador verificar que sea el cuidador asignado al tratamiento o al paciente actualmente
        if (!isAdmin) {
            Long cuidadorId = usuarioService.findByEmail(authentication.getName()).get().getIdUsuario();
            boolean esCuidadorDelTratamiento = tratamiento.getCuidadorId().equals(cuidadorId);
            boolean esAsignadoActualAlPaciente = pacienteAsignadoRepository
                    .findByCuidador_idUsuarioAndPaciente_idPacienteAndEstado(
                            cuidadorId, tratamiento.getPacienteId(), EstadoAsignacion.Activo)
                    .isPresent();

            if (!esCuidadorDelTratamiento && !esAsignadoActualAlPaciente) {
                redirectAttributes.addFlashAttribute("errorMessage", "No tienes permiso para editar este tratamiento.");
                return "redirect:/tratamientos/mis-tratamientos";
            }
        }


        model.addAttribute("tratamiento", tratamiento);
        model.addAttribute("isAdmin", isAdmin);
        if (isAdmin) {
            // Admin lista de pacientes y cuidadores
            model.addAttribute("pacientes", Collections.singletonList(pacienteService.obtenerPacientePorId(tratamiento.getPacienteId()).orElse(null))); // Solo paciente actual
            model.addAttribute("cuidadores", usuarioService.findByRol(RolNombre.Cuidador));
        }

        return "formulario-tratamiento-editar";
    }


    @PostMapping("/actualizar/{id}")
    @PreAuthorize("hasAnyRole('Administrador', 'Cuidador')")
    public String actualizarTratamiento(@PathVariable Long id,
                                        @Valid @ModelAttribute("tratamiento") TratamientoDTO tratamientoDTO,
                                        BindingResult bindingResult, Authentication authentication,
                                        Model model, RedirectAttributes redirectAttributes) {

        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_Administrador"));
        Long usuarioId = usuarioService.findByEmail(authentication.getName()).get().getIdUsuario();

        // Si es Cuidador solo puede actualizar observaciones
        if (!isAdmin && bindingResult.hasErrors()) {
            // Permitir continuar si los errores no son de observaciones
            List<String> nonObservationErrors = bindingResult.getFieldErrors().stream()
                    .map(fe -> fe.getField())
                    .filter(field -> !field.equals("observaciones"))
                    .collect(Collectors.toList());

            if (!nonObservationErrors.isEmpty()) {
                // Si hay errores en otros campos siendo cuidador recargar
                model.addAttribute("tratamiento", tratamientoDTO); // Devolver DTO con errores
                model.addAttribute("isAdmin", isAdmin);
                // No recargar listas el cuidador no las ve en el form edit
                return "formulario-tratamiento-editar";
            }
            // Si solo hay error en observaciones mostrar
        } else if (isAdmin && bindingResult.hasErrors()) {
            // Si es Admin y hay errores recargar
            model.addAttribute("tratamiento", tratamientoDTO);
            model.addAttribute("isAdmin", isAdmin);
            model.addAttribute("pacientes", Collections.singletonList(pacienteService.obtenerPacientePorId(tratamientoDTO.getPacienteId()).orElse(null)));
            model.addAttribute("cuidadores", usuarioService.findByRol(RolNombre.Cuidador));
            return "formulario-tratamiento-editar";
        }


        try {
            if (isAdmin) {
                tratamientoService.actualizarTratamientoAdmin(id, tratamientoDTO);
            } else {
                // Cuidador solo actualiza observaciones
                tratamientoService.actualizarObservacionesCuidador(id, usuarioId, tratamientoDTO.getObservaciones());
            }
            redirectAttributes.addFlashAttribute("successMessage", "¡Tratamiento actualizado con éxito!");

            // Redirigir según rol
            return isAdmin ? "redirect:/tratamientos/admin" : "redirect:/tratamientos/mis-tratamientos";

        } catch (AccessDeniedException | IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/tratamientos/editar/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar el tratamiento: " + e.getMessage());
            return "redirect:/tratamientos/editar/" + id;
        }
    }


    @PostMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('Administrador')")
    public String eliminarTratamiento(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            tratamientoService.eliminarTratamientoLogico(id);
            redirectAttributes.addFlashAttribute("successMessage", "¡Tratamiento eliminado con éxito!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el tratamiento.");
        }
        return "redirect:/tratamientos/admin";
    }

    // Vista/Acciones Cuidador

    @GetMapping("/mis-tratamientos")
    @PreAuthorize("hasRole('Cuidador')")
    public String listarMisTratamientos(Authentication authentication, Model model) {
        Long cuidadorId = usuarioService.findByEmail(authentication.getName()).get().getIdUsuario();
        model.addAttribute("tratamientos", tratamientoService.listarTratamientosActivosPorCuidador(cuidadorId));
        return "mis-tratamientos"; // Nueva vista
    }

    @PostMapping("/completar/{id}")
    @PreAuthorize("hasRole('Cuidador')")
    public String completarTratamiento(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        Long cuidadorId = usuarioService.findByEmail(authentication.getName()).get().getIdUsuario();
        try {
            tratamientoService.completarTratamiento(id, cuidadorId);
            redirectAttributes.addFlashAttribute("successMessage", "Tratamiento #" + id + " marcado como completado.");
        } catch (AccessDeniedException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al completar el tratamiento.");
        }
        return "redirect:/tratamientos/mis-tratamientos";
    }


    // Vista Familiar

    @GetMapping("/paciente/{pacienteId}")
    @PreAuthorize("hasRole('Familiar')")
    public String listarTratamientosPacienteFamiliar(@PathVariable Long pacienteId, Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        // Validar que el familiar esté asociado a este paciente
        boolean asociado = usuarioService.findPacientesByFamiliarEmail(authentication.getName())
                .stream()
                .anyMatch(pa -> pa.getPaciente().getIdPaciente().equals(pacienteId));

        if (!asociado) {
            redirectAttributes.addFlashAttribute("errorMessage", "No tienes permiso para ver los tratamientos de este paciente.");
            return "redirect:/dashboard";
        }

        model.addAttribute("tratamientos", tratamientoService.listarTratamientosActivosPorPaciente(pacienteId));
        model.addAttribute("pacienteNombre", pacienteService.obtenerPacientePorId(pacienteId).map(p -> p.getNombre() + " " + p.getApellido()).orElse("Desconocido"));
        return "tratamientos-paciente";
    }
}