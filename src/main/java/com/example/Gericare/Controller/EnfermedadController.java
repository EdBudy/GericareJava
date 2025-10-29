package com.example.Gericare.Controller;

import com.example.Gericare.DTO.EnfermedadDTO;
import com.example.Gericare.Service.EnfermedadService; // Correcto: Servicio dedicado
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/enfermedades") // Ruta base para enfermedades
@PreAuthorize("hasRole('Administrador')") // Solo Admin
public class EnfermedadController {

    private static final Logger log = LoggerFactory.getLogger(EnfermedadController.class);

    @Autowired
    private EnfermedadService enfermedadService; // Inyecta el servicio dedicado

    /**
     * Muestra la página de gestión del catálogo de enfermedades.
     */
    @GetMapping
    public String listarEnfermedades(Model model) {
        log.info("Accediendo a la lista de enfermedades");
        try {
            model.addAttribute("enfermedades", enfermedadService.listarEnfermedadesActivas());
        } catch (Exception e) {
            log.error("Error al listar enfermedades", e);
            model.addAttribute("errorMessage", "Error al cargar la lista de enfermedades.");
            model.addAttribute("enfermedades", List.of()); // Lista vacía
        }
        return "gestion-enfermedades"; // Nombre de la vista Thymeleaf
    }

    /**
     * Muestra el formulario para crear una nueva enfermedad.
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        log.debug("Mostrando formulario para nueva enfermedad");
        if (!model.containsAttribute("enfermedad")) {
            model.addAttribute("enfermedad", new EnfermedadDTO());
        }
        return "formulario-enfermedad"; // Nombre de la vista Thymeleaf
    }

    /**
     * Muestra el formulario para editar una enfermedad existente.
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        log.debug("Mostrando formulario para editar enfermedad ID: {}", id);
        return enfermedadService.obtenerEnfermedadPorId(id)
                .map(enf -> {
                    model.addAttribute("enfermedad", enf);
                    return "formulario-enfermedad"; // Reutiliza la misma vista
                })
                .orElseGet(() -> {
                    log.warn("Enfermedad ID: {} no encontrada para editar", id);
                    redirectAttributes.addFlashAttribute("errorMessage", "Enfermedad no encontrada.");
                    return "redirect:/enfermedades";
                });
    }

    /**
     * Procesa el guardado (creación o actualización) de una enfermedad desde el formulario principal.
     */
    @PostMapping("/guardar")
    public String guardarEnfermedad(@Valid @ModelAttribute("enfermedad") EnfermedadDTO enfermedadDTO,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes,
                                    Model model) {

        Long id = enfermedadDTO.getIdEnfermedad();
        log.info("Intentando guardar enfermedad ID: {}", id != null ? id : "nueva");

        if (bindingResult.hasErrors()) {
            log.warn("Errores de validación al guardar enfermedad: {}", bindingResult.getAllErrors());
            model.addAttribute("enfermedad", enfermedadDTO); // Mantener datos en el modelo
            return "formulario-enfermedad"; // Volver al formulario
        }

        try {
            enfermedadService.guardarEnfermedad(enfermedadDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Enfermedad " + (id != null ? "actualizada" : "creada") + " correctamente.");
            log.info("Enfermedad ID: {} guardada exitosamente.", id != null ? id : "nueva");
            return "redirect:/enfermedades"; // Redirige a la lista

        } catch (RuntimeException e) { // Captura errores específicos como IllegalArgumentException o duplicados
            log.error("Error al guardar enfermedad ID: {}: {}", id != null ? id : "nueva", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error al guardar: " + e.getMessage());
            redirectAttributes.addFlashAttribute("enfermedad", enfermedadDTO);
            if (id != null) {
                return "redirect:/enfermedades/editar/" + id;
            } else {
                return "redirect:/enfermedades/nuevo";
            }
        } catch (Exception e) { // Captura errores inesperados (checked exceptions)
            log.error("Error inesperado al guardar enfermedad ID: {}", id != null ? id : "nueva", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ocurrió un error inesperado al guardar la enfermedad.");
            redirectAttributes.addFlashAttribute("enfermedad", enfermedadDTO);
            if (id != null) {
                return "redirect:/enfermedades/editar/" + id;
            } else {
                return "redirect:/enfermedades/nuevo";
            }
        }
    }

    /**
     * Procesa la creación de una nueva enfermedad vía AJAX (desde el modal).
     * Responde con JSON.
     */
    @PostMapping("/nuevo-ajax")
    @ResponseBody
    public ResponseEntity<?> guardarEnfermedadAjax(@RequestBody EnfermedadDTO enfermedadDTO) {
        log.info("Recibida petición AJAX para guardar nueva enfermedad: {}", enfermedadDTO.getNombreEnfermedad());
        if (enfermedadDTO.getNombreEnfermedad() == null || enfermedadDTO.getNombreEnfermedad().isBlank()) {
            log.warn("Intento de guardar enfermedad vía AJAX con nombre vacío.");
            return ResponseEntity.badRequest().body("El nombre de la enfermedad no puede estar vacío.");
        }
        try {
            EnfermedadDTO guardada = enfermedadService.guardarEnfermedad(enfermedadDTO);
            log.info("Enfermedad guardada vía AJAX con ID: {}", guardada.getIdEnfermedad());
            return ResponseEntity.ok(guardada);
        } catch (RuntimeException e){ // Captura errores conocidos (ej: duplicado)
            log.error("Error conocido AJAX al guardar enfermedad '{}': {}", enfermedadDTO.getNombreEnfermedad(), e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado AJAX al guardar enfermedad '{}'", enfermedadDTO.getNombreEnfermedad(), e);
            return ResponseEntity.internalServerError().body("Error interno al guardar la enfermedad.");
        }
    }

    /**
     * Procesa el borrado lógico (inactivación) de una enfermedad.
     */
    @PostMapping("/eliminar/{id}")
    public String eliminarEnfermedad(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("Intentando eliminar (inactivar) enfermedad ID: {}", id);
        try {
            enfermedadService.eliminarEnfermedad(id);
            redirectAttributes.addFlashAttribute("successMessage", "Enfermedad inactivada correctamente.");
            log.info("Enfermedad ID: {} inactivada exitosamente.", id);
        } catch (Exception e) {
            log.error("Error al inactivar enfermedad ID: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error al inactivar la enfermedad: " + e.getMessage());
        }
        return "redirect:/enfermedades";
    }

    /**
     * Endpoint AJAX para obtener la lista de enfermedades activas.
     * Responde con JSON.
     */
    @GetMapping("/listar-activos-ajax")
    @ResponseBody
    public ResponseEntity<List<EnfermedadDTO>> listarActivosAjax() {
        log.debug("Recibida petición AJAX para listar enfermedades activas");
        try {
            List<EnfermedadDTO> lista = enfermedadService.listarEnfermedadesActivas();
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            log.error("Error AJAX al listar enfermedades activas", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}