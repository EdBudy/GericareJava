package com.example.Gericare.Controller;

import com.example.Gericare.DTO.PacienteDTO;
import com.example.Gericare.Service.PacienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/pacientes")
public class PacienteController {

    @Autowired
    private PacienteService pacienteService;

    // --- MÉTODOS PARA PROCESAR ACCIONES (POST, GET con acción) ---
    // Agrupar todos los métodos que procesan datos de un formulario o realizan una acción.

    /**
     * Procesa los datos enviados desde el formulario de creación.
     */
    @PostMapping("/crear")
    public String crearPaciente(@ModelAttribute("paciente") PacienteDTO pacienteDTO, RedirectAttributes redirectAttributes) {
        pacienteService.crearPaciente(pacienteDTO);
        redirectAttributes.addFlashAttribute("mensaje", "Paciente registrado exitosamente");
        return "redirect:/pacientes/lista";
    }

    /**
     * Procesa los datos enviados desde el formulario de edición.
     */
    @PostMapping("/editar/{id}")
    public String actualizarPaciente(@PathVariable Long id, @ModelAttribute("paciente") PacienteDTO pacienteDTO, RedirectAttributes redirectAttributes) {
        pacienteService.actualizarPaciente(id, pacienteDTO);
        redirectAttributes.addFlashAttribute("mensaje", "Paciente actualizado exitosamente");
        return "redirect:/pacientes/lista";
    }

    /**
     * Procesa la acción de eliminar (desactivar) un paciente.
     */
    @GetMapping("/eliminar/{id}")
    public String eliminarPaciente(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        pacienteService.eliminarPaciente(id);
        redirectAttributes.addFlashAttribute("mensaje", "Paciente desactivado exitosamente");
        return "redirect:/pacientes/lista";
    }


    // --- MÉTODOS PARA MOSTRAR VISTAS (GET) ---
    // Agrupar todos los métodos que devuelven una página HTML.

    /**
     * Muestra la página principal con la lista de todos los pacientes.
     */
    @GetMapping("/lista")
    public String listarPacientes(Model model) {
        List<PacienteDTO> listaPacientes = pacienteService.listarTodosLosPacientes();
        model.addAttribute("pacientes", listaPacientes);
        return "admin/pacientes/gestionPacientes";
    }

    /**
     * Muestra el formulario vacío para registrar un nuevo paciente.
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioDeNuevoPaciente(Model model) {
        model.addAttribute("paciente", new PacienteDTO());
        return "admin/pacientes/formPaciente";
    }

    /**
     * Muestra el formulario con los datos de un paciente existente para editarlo.
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioDeEdicion(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return pacienteService.obtenerPacientePorId(id)
                .map(paciente -> {
                    model.addAttribute("paciente", paciente);
                    return "admin/pacientes/formEdicionPaciente"; // Vista para editar
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Error: Paciente no encontrado.");
                    return "redirect:/pacientes/lista";
                });
    }
}