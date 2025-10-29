package com.example.Gericare.Controller;

import com.example.Gericare.DTO.HistoriaClinicaDTO;
import com.example.Gericare.Entity.Paciente;
import com.example.Gericare.Repository.PacienteRepository;
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
import java.util.Optional;

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
    @Autowired
    private PacienteRepository pacienteRepository;

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

    @GetMapping("/editar/paciente/{pacienteId}")
    @PreAuthorize("hasRole('Administrador')")
    public String mostrarFormularioEditar(@PathVariable Long pacienteId, Model model, RedirectAttributes redirectAttributes) {
        log.info("Mostrando formulario de edición/creación para HC del paciente ID: {}", pacienteId);


        // Primero, intentar obtener la HC existente
        Optional<HistoriaClinicaDTO> hcExistenteOpt = historiaClinicaService.obtenerHistoriaClinicaPorPacienteId(pacienteId);

        if (hcExistenteOpt.isPresent()) {
            // --- Caso 1: SI LA HC EXISTE (Editar) ---
            model.addAttribute("historia", hcExistenteOpt.get());
            log.debug("HC encontrada para paciente ID: {}. Mostrando formulario de edición.", pacienteId);
        } else {
            // --- Caso 2: SI LA HC NO EXISTE (Crear) ---
            log.debug("No se encontró HC para paciente ID: {}. Preparando formulario de creación.", pacienteId);

            // Verificar que el paciente (al que le crearemos la HC) exista
            // (Asegúrate de tener PacienteRepository inyectado en este controlador)
            Optional<Paciente> pacienteOpt = pacienteRepository.findById(pacienteId);

            if (pacienteOpt.isEmpty()) {
                // Si ni siquiera el paciente existe, redirigir con error
                log.error("Intento de crear HC para Paciente ID {} que no existe.", pacienteId);
                redirectAttributes.addFlashAttribute("errorMessage", "Paciente no encontrado con ID: " + pacienteId);
                return "redirect:/pacientes"; // Vuelve a la lista de pacientes
            }

            // Si el paciente existe, crear un DTO vacío para el formulario
            Paciente paciente = pacienteOpt.get();
            HistoriaClinicaDTO nuevaHc = new HistoriaClinicaDTO();
            nuevaHc.setIdPaciente(paciente.getIdPaciente());
            nuevaHc.setNombrePacienteCompleto(paciente.getNombre() + " " + paciente.getApellido());
            // El idHistoriaClinica (nuevaHc.getIdHistoriaClinica()) es null por defecto

            model.addAttribute("historia", nuevaHc);
        }

        // --- Paso 2: Cargar Catálogos (SIEMPRE se ejecuta) ---
        // Este bloque ahora se alcanza en ambos casos (crear o editar)
        try {
            model.addAttribute("medicamentosCatalogo", medicamentoService.listarMedicamentosActivos());
            model.addAttribute("enfermedadesCatalogo", enfermedadService.listarEnfermedadesActivas());
            log.debug("Catálogos cargados para el formulario de HC del paciente ID: {}", pacienteId);
        } catch (Exception e) {
            log.error("Error al cargar catálogos para el formulario de HC del paciente ID: {}", pacienteId, e);
            model.addAttribute("medicamentosCatalogo", List.of()); // Añadir listas vacías
            model.addAttribute("enfermedadesCatalogo", List.of()); // Añadir listas vacías
            model.addAttribute("formError", "Error al cargar listas de medicamentos/enfermedades.");
        }

        // --- Paso 3: Devolver la vista ---
        return "formulario-historia"; // Nombre de la vista Thymeleaf
    }

    @PostMapping("/editar/paciente/{pacienteId}") // Se usa el ID del paciente en la URL
    @PreAuthorize("hasRole('Administrador')")
    public String actualizarHistoriaClinica(@PathVariable Long pacienteId, // Recibe el ID del paciente
                                            @ModelAttribute("historia") HistoriaClinicaDTO historiaClinicaDTO,
                                            BindingResult bindingResult, // Capturar errores de validación
                                            Authentication authentication,
                                            RedirectAttributes redirectAttributes,
                                            Model model) { // Para devolver errores al form si es necesario

        log.info("Recibido POST para guardar HC del paciente ID: {}", pacienteId);

        try {
            // Obtener ID del admin autenticado de forma segura
            Long adminId = usuarioService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new AccessDeniedException("Usuario administrador no autenticado correctamente."))
                    .getIdUsuario();

            // Obtener ID de la Historia Clínica desde el DTO que viene del formulario
            // Si HC = nueva, ID = null. Si ys existe tendrá valor
            Long hcId = historiaClinicaDTO.getIdHistoriaClinica();

            // Servicio HistoriaClinicaServiceImpl se encarga de determinar si crear HC o actualizar
            historiaClinicaService.actualizarHistoriaClinica(hcId, historiaClinicaDTO, adminId);

            redirectAttributes.addFlashAttribute("successMessage", "Historia Clínica guardada correctamente.");
            log.info("HC del paciente ID: {} guardada exitosamente.", pacienteId);
            // Redirigir a la vista de la HC (usa el ID del paciente que ya está en el DTO)
            return "redirect:/historias-clinicas/paciente/" + historiaClinicaDTO.getIdPaciente();

        } catch (RuntimeException e) {
            log.error("Error de lógica al guardar HC del paciente ID: {}: {}", pacienteId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error al guardar: " + e.getMessage());
            // Reenvía el DTO con los datos que causaron el error
            redirectAttributes.addFlashAttribute("historia", historiaClinicaDTO);
            return "redirect:/historias-clinicas/editar/paciente/" + pacienteId;

        } catch (Exception e){ // Captura cualquier otro error inesperado
            log.error("Error inesperado al guardar HC del paciente ID: {}", pacienteId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ocurrió un error inesperado al guardar la historia clínica.");
            redirectAttributes.addFlashAttribute("historia", historiaClinicaDTO);
            return "redirect:/historias-clinicas/editar/paciente/" + pacienteId;
        }
    }
}