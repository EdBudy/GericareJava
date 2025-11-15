package com.example.Gericare.Controller;

import com.example.Gericare.DTO.MedicamentoDTO;
import com.example.Gericare.Service.MedicamentoService;
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
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.util.Map;

import java.util.List;

@Controller
@RequestMapping("/medicamentos") // Ruta base para este controlador
@PreAuthorize("hasRole('Administrador')") // Asegura que solo Admins accedan
public class MedicamentoController {

    private static final Logger log = LoggerFactory.getLogger(MedicamentoController.class);

    @Autowired
    private MedicamentoService medicamentoService; // Inyecta el servicio dedicado

    @GetMapping
    public String listarMedicamentos(Model model,
                                     @RequestParam(required = false) String nombre,
                                     @RequestParam(required = false) String descripcion) {
        log.info("Accediendo a la lista de medicamentos");
        try {
            model.addAttribute("medicamentos", medicamentoService.listarMedicamentosActivosFiltrados(nombre, descripcion));
        } catch (Exception e) {
            log.error("Error al listar medicamentos", e);
            model.addAttribute("errorMessage", "Error al cargar la lista de medicamentos.");
            // Añadir lista vacía para evitar errores en Thymeleaf si 'medicamentos' no existe
            model.addAttribute("medicamentos", List.of());
        }
        return "historia/admin-medicamentos-gestion";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        log.debug("Mostrando formulario para nuevo medicamento");
        // Asegura que siempre haya un objeto 'medicamento' en el modelo para el form
        if (!model.containsAttribute("medicamento")) {
            model.addAttribute("medicamento", new MedicamentoDTO());
        }
        return "historia/admin-medicamentos-formulario";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        log.debug("Mostrando formulario para editar medicamento ID: {}", id);
        return medicamentoService.obtenerMedicamentoPorId(id)
                .map(med -> {
                    model.addAttribute("medicamento", med);
                    return "historia/admin-medicamentos-formulario";
                })
                .orElseGet(() -> {
                    log.warn("Medicamento ID: {} no encontrado para editar", id);
                    redirectAttributes.addFlashAttribute("errorMessage", "Medicamento no encontrado.");
                    return "redirect:/medicamentos";
                });
    }

    @PostMapping("/guardar")
    public String guardarMedicamento(@Valid @ModelAttribute("medicamento") MedicamentoDTO medicamentoDTO,
                                     BindingResult bindingResult, // Captura errores de validación (si añades @Valid y anotaciones en DTO)
                                     RedirectAttributes redirectAttributes,
                                     Model model) { // Model para devolver al form si hay errores

        Long id = medicamentoDTO.getIdMedicamento();
        log.info("Intentando guardar medicamento ID: {}", id != null ? id : "nuevo");

        // Si hay errores de validación definidos en el DTO (ej. @NotBlank), vuelve al formulario
        if (bindingResult.hasErrors()) {
            log.warn("Errores de validación al guardar medicamento: {}", bindingResult.getAllErrors());
            // No usamos redirectAttributes aquí, añadimos directamente al modelo
            // para que Thymeleaf muestre los errores en el mismo formulario.
            // Aseguramos que el objeto 'medicamento' con errores se mantenga.
            model.addAttribute("medicamento", medicamentoDTO);
            return "historia/admin-medicamentos-formulario";
        }

        try {
            medicamentoService.guardarMedicamento(medicamentoDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Medicamento " + (id != null ? "actualizado" : "creado") + " correctamente.");
            log.info("Medicamento ID: {} guardado exitosamente.", id != null ? id : "nuevo");
            return "redirect:/medicamentos"; // Redirige a la lista

        } catch (RuntimeException e) { // Captura errores específicos del servicio (ej. nombre vacío, duplicado)
            log.error("Error al guardar medicamento ID: {}: {}", id != null ? id : "nuevo", e.getMessage());
            // Usamos redirectAttributes para mostrar el error DESPUÉS de redirigir
            redirectAttributes.addFlashAttribute("errorMessage", "Error al guardar: " + e.getMessage());
            redirectAttributes.addFlashAttribute("medicamento", medicamentoDTO); // Reenvía los datos ingresados
            // Redirige al formulario correspondiente (nuevo o editar)
            if (id != null) {
                return "redirect:/medicamentos/editar/" + id;
            } else {
                return "redirect:/medicamentos/nuevo";
            }
        } catch (Exception e) { // Captura cualquier otro error inesperado (checked exceptions)
            log.error("Error inesperado al guardar medicamento ID: {}", id != null ? id : "nuevo", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ocurrió un error inesperado al guardar el medicamento.");
            redirectAttributes.addFlashAttribute("medicamento", medicamentoDTO);
            if (id != null) {
                return "redirect:/medicamentos/editar/" + id;
            } else {
                return "redirect:/medicamentos/nuevo";
            }
        }
    }

    @PostMapping("/nuevo-ajax")
    @ResponseBody // Importante: indica que la respuesta es el cuerpo, no una vista
    public ResponseEntity<?> guardarMedicamentoAjax(@RequestBody MedicamentoDTO medicamentoDTO) {
        log.info("Recibida petición AJAX para guardar nuevo medicamento: {}", medicamentoDTO.getNombreMedicamento());
        // Validación simple aquí (podrías usar @Valid si configuras manejo de errores para @RequestBody)
        if (medicamentoDTO.getNombreMedicamento() == null || medicamentoDTO.getNombreMedicamento().isBlank()) {
            log.warn("Intento de guardar medicamento vía AJAX con nombre vacío.");
            return ResponseEntity.badRequest().body("El nombre del medicamento no puede estar vacío."); // Error 400
        }
        try {
            // Llama al servicio para guardar
            MedicamentoDTO guardado = medicamentoService.guardarMedicamento(medicamentoDTO);
            log.info("Medicamento guardado vía AJAX con ID: {}", guardado.getIdMedicamento());
            return ResponseEntity.ok(guardado); // Éxito: Devuelve el DTO guardado (con ID) como JSON
        } catch (RuntimeException e){ // Captura errores conocidos (ej: duplicado)
            log.error("Error conocido AJAX al guardar medicamento '{}': {}", medicamentoDTO.getNombreMedicamento(), e.getMessage());
            // Devuelve el mensaje de error específico del servicio
            return ResponseEntity.badRequest().body(e.getMessage()); // Error 400 u otro apropiado
        } catch (Exception e) {
            log.error("Error inesperado AJAX al guardar medicamento '{}'", medicamentoDTO.getNombreMedicamento(), e);
            // Error genérico para el cliente
            return ResponseEntity.internalServerError().body("Error interno al guardar el medicamento."); // Error 500
        }
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarMedicamento(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("Intentando eliminar medicamento ID: {}", id);
        try {
            medicamentoService.eliminarMedicamento(id);
            redirectAttributes.addFlashAttribute("successMessage", "Medicamento eliminado correctamente.");
            log.info("Medicamento ID: {} eliminado exitosamente.", id);
        } catch (Exception e) {
            log.error("Error al eliminar medicamento ID: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el medicamento: " + e.getMessage());
        }
        return "redirect:/medicamentos"; // Vuelve a la lista
    }

    @GetMapping("/listar-activos-ajax")
    @ResponseBody
    public ResponseEntity<List<MedicamentoDTO>> listarActivosAjax() {
        log.debug("Recibida petición AJAX para listar medicamentos activos");
        try {
            List<MedicamentoDTO> lista = medicamentoService.listarMedicamentosActivos();
            return ResponseEntity.ok(lista); // Devuelve la lista como JSON
        } catch (Exception e) {
            log.error("Error AJAX al listar medicamentos activos", e);
            return ResponseEntity.internalServerError().build(); // Error 500 sin cuerpo
        }
    }

    // Carga masiva de datos medicamentos
    @PostMapping("/cargar-excel")
    public String cargarMedicamentosExcel(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Por favor, seleccione un archivo Excel para cargar.");
            return "redirect:/medicamentos";
        }

        try (InputStream inputStream = file.getInputStream()) {
            // Llamar al servicio que devuelve estadísticas
            Map<String, Integer> resultado = medicamentoService.cargarDesdeExcel(inputStream);

            // Mensaje de éxito informativo
            String successMsg = String.format(
                    "Carga completada. Filas procesadas: %d. Nuevos guardados: %d. Duplicados omitidos: %d.",
                    resultado.get("total"),
                    resultado.get("guardados"),
                    resultado.get("omitidos")
            );

            redirectAttributes.addFlashAttribute("successMessage", successMsg);

        } catch (Exception e) {
            log.error("Error al cargar archivo Excel de medicamentos", e);
            // Mensaje de error para formato de archivo incorrecto/otros problemas
            redirectAttributes.addFlashAttribute("errorMessage", "Error al procesar el archivo: " + e.getMessage());
        }
        return "redirect:/medicamentos";
    }
}