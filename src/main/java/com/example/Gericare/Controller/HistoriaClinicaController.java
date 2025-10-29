package com.example.Gericare.Controller;

import com.example.Gericare.DTO.HistoriaClinicaDTO;
import com.example.Gericare.Service.EnfermedadService; // Necesario
import com.example.Gericare.Service.HistoriaClinicaService;
import com.example.Gericare.Service.MedicamentoService; // Necesario
import com.example.Gericare.Service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException; // Para control de acceso si es necesario
import org.springframework.security.access.prepost.PreAuthorize; // Alternativa para seguridad a nivel de metodo
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority; // Para verificar roles
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult; // Para validación de formularios
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/historias-clinicas")
// Seguridad a nivel de clase si ambos roles pueden acceder a la mayoría de métodos
// @PreAuthorize("hasAnyRole('Administrador', 'Cuidador')") // Ejemplo si cuidador puede ver
public class HistoriaClinicaController {

    private static final Logger log = LoggerFactory.getLogger(HistoriaClinicaController.class);

    @Autowired
    private HistoriaClinicaService historiaClinicaService;
    @Autowired
    private UsuarioService usuarioService; // Para obtener ID del admin logueado
    @Autowired
    private MedicamentoService medicamentoService; // Para obtener catálogo
    @Autowired
    private EnfermedadService enfermedadService; // Para obtener catálogo

    /**
     * Muestra la vista de solo lectura de la Historia Clínica por ID de Paciente.
     * Accesible por Admin y Cuidador (según SecurityConfig).
     */
    @GetMapping("/paciente/{pacienteId}")
    public String verHistoriaClinicaPorPaciente(@PathVariable Long pacienteId, Model model,
                                                Authentication authentication, // Para verificar rol si es necesario aquí
                                                RedirectAttributes redirectAttributes) {
        log.info("Accediendo a HC para paciente ID: {}", pacienteId);

        return historiaClinicaService.obtenerHistoriaClinicaPorPacienteId(pacienteId)
                .map(hc -> {
                    log.debug("HC encontrada para paciente ID: {}", pacienteId);
                    model.addAttribute("historia", hc);
                    // No necesita catálogos aquí, es solo vista
                    return "ver-historia"; // Nombre de la vista Thymeleaf
                })
                .orElseGet(() -> {
                    // Esto podría pasar si la creación inicial falló o fue eliminada incorrectamente
                    log.warn("No se encontró HC para paciente ID: {}", pacienteId);
                    redirectAttributes.addFlashAttribute("errorMessage", "No se encontró historia clínica para el paciente con ID: " + pacienteId);
                    return "redirect:/pacientes"; // Volver a la lista
                });
    }

    /**
     * Muestra el formulario para EDITAR una Historia Clínica existente.
     * Solo accesible por Administrador (asegurado por SecurityConfig o @PreAuthorize).
     */
    @GetMapping("/editar/{id}")
    @PreAuthorize("hasRole('Administrador')") // Seguridad a nivel de metodo
    public String mostrarFormularioEditar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        log.info("Mostrando formulario de edición para HC ID: {}", id);
        return historiaClinicaService.obtenerHistoriaClinicaPorId(id)
                .map(hc -> {
                    model.addAttribute("historia", hc);
                    // Cargar catálogos para los <select> del formulario
                    try {
                        model.addAttribute("medicamentosCatalogo", medicamentoService.listarMedicamentosActivos());
                        model.addAttribute("enfermedadesCatalogo", enfermedadService.listarEnfermedadesActivas());
                        log.debug("Catálogos cargados para el formulario de HC ID: {}", id);
                    } catch (Exception e) {
                        log.error("Error al cargar catálogos para el formulario de HC ID: {}", id, e);
                        // Añadir listas vacías para evitar error Thymeleaf y mostrar mensaje
                        model.addAttribute("medicamentosCatalogo", List.of());
                        model.addAttribute("enfermedadesCatalogo", List.of());
                        model.addAttribute("formError", "Error al cargar listas de medicamentos/enfermedades."); // Mensaje en el form
                    }
                    return "formulario-historia"; // Nombre de la vista Thymeleaf
                })
                .orElseGet(() -> {
                    log.warn("HC ID: {} no encontrada para editar", id);
                    redirectAttributes.addFlashAttribute("errorMessage", "Historia clínica no encontrada con ID: " + id);
                    return "redirect:/pacientes"; // O a donde tenga sentido
                });
    }

    /**
     * Procesa la ACTUALIZACIÓN de la Historia Clínica desde el formulario.
     * Solo accesible por Administrador.
     */
    @PostMapping("/editar/{id}")
    @PreAuthorize("hasRole('Administrador')")
    public String actualizarHistoriaClinica(@PathVariable Long id,
                                            @ModelAttribute("historia") /*@Valid*/ HistoriaClinicaDTO historiaClinicaDTO, // Añadir @Valid si pones validaciones en DTO
                                            BindingResult bindingResult, // Capturar errores de validación
                                            Authentication authentication,
                                            RedirectAttributes redirectAttributes,
                                            Model model) { // Para devolver errores al form si es necesario

        log.info("Intentando actualizar HC ID: {}", id);

        // Importante: Si usas @Valid, necesitas manejar bindingResult ANTES de llamar al servicio
        if (bindingResult.hasErrors()) {
            log.warn("Errores de validación al actualizar HC ID: {}: {}", id, bindingResult.getAllErrors());
            // Recargar catálogos y volver a mostrar el formulario con errores
            try {
                model.addAttribute("medicamentosCatalogo", medicamentoService.listarMedicamentosActivos());
                model.addAttribute("enfermedadesCatalogo", enfermedadService.listarEnfermedadesActivas());
            } catch (Exception e) {
                log.error("Error recargando catálogos tras error de validación para HC ID: {}", id, e);
                model.addAttribute("formError", "Error al cargar listas desplegables.");
            }
            model.addAttribute("historia", historiaClinicaDTO); // Mantener datos ingresados
            return "formulario-historia"; // Nombre de la vista del formulario
        }


        try {
            // Obtener ID del admin autenticado de forma segura
            Long adminId = usuarioService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new AccessDeniedException("Usuario administrador no autenticado correctamente."))
                    .getIdUsuario();

            // Llamar al servicio para actualizar
            historiaClinicaService.actualizarHistoriaClinica(id, historiaClinicaDTO, adminId);

            redirectAttributes.addFlashAttribute("successMessage", "Historia Clínica actualizada correctamente.");
            log.info("HC ID: {} actualizada exitosamente.", id);
            // Redirigir a la vista de la HC actualizada
            return "redirect:/historias-clinicas/paciente/" + historiaClinicaDTO.getIdPaciente(); // Usa el ID de paciente del DTO

        } catch (RuntimeException e) { // Captura errores esperados del servicio (ej. no encontrado)
            log.error("Error de lógica al actualizar HC ID: {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar: " + e.getMessage());
            // Reenvía el DTO con los datos que causaron el error
            redirectAttributes.addFlashAttribute("historia", historiaClinicaDTO);
            return "redirect:/historias-clinicas/editar/" + id; // Vuelve al form de edición

        } catch (Exception e){ // Captura cualquier otro error inesperado
            log.error("Error inesperado al actualizar HC ID: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ocurrió un error inesperado al actualizar la historia clínica.");
            redirectAttributes.addFlashAttribute("historia", historiaClinicaDTO);
            return "redirect:/historias-clinicas/editar/" + id;
        }
    }
}