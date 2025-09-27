package com.example.Gericare.Controller;

import com.example.Gericare.DTO.PacienteDTO;
import com.example.Gericare.Service.PacienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 1. CAMBIO CLAVE: De @RestController a @Controller para que pueda devolver vistas.
@Controller
@RequestMapping("/pacientes")
public class PacienteController {

    @Autowired
    private PacienteService pacienteService;

    // --- MÉTODOS AÑADIDOS PARA EL FRONTEND (THYMELEAF) ---

    @GetMapping
    public String listarPacientes(Model model) {
        // Añade la lista de pacientes al modelo para que la vista pueda usarla.
        model.addAttribute("pacientes", pacienteService.listarTodosLosPacientes());
        return "gestion-pacientes"; // Devuelve "gestion-pacientes.html"
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevoPaciente(Model model) {
        // Se crea un objeto vacío para que el formulario de Thymeleaf (`th:object`) pueda enlazar los campos.
        model.addAttribute("paciente", new PacienteDTO());
        return "formulario-paciente"; // Devuelve "formulario-paciente.html"
    }

    @PostMapping("/crear")
    public String crearPaciente(PacienteDTO pacienteDTO) {
        pacienteService.crearPaciente(pacienteDTO);
        // Redirige a la URL /pacientes para mostrar la lista actualizada.
        return "redirect:/pacientes";
    }

    // --- MÉTODOS ANTIGUOS (API REST) ---
    // Se mantienen por si se necesitan en el futuro, pero se agrupan en /api
    // y se les añade @ResponseBody para que sigan devolviendo JSON.

    @GetMapping("/api")
    @ResponseBody
    public List<PacienteDTO> listarTodosLosPacientesApi() {
        return pacienteService.listarTodosLosPacientes();
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<PacienteDTO> obtenerPacientePorIdApi(@PathVariable Long id) {
        return pacienteService.obtenerPacientePorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<PacienteDTO> actualizarPacienteApi(@PathVariable Long id, @RequestBody PacienteDTO pacienteDTO) {
        return pacienteService.actualizarPaciente(id, pacienteDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Void> eliminarPacienteApi(@PathVariable Long id) {
        if (pacienteService.obtenerPacientePorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        pacienteService.eliminarPaciente(id);
        return ResponseEntity.noContent().build();
    }
}