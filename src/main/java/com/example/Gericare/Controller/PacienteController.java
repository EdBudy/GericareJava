package com.example.Gericare.Controller;

import com.example.Gericare.DTO.PacienteDTO;
import com.example.Gericare.Service.PacienteService;
import com.example.Gericare.Service.UsuarioService;
import com.example.Gericare.enums.RolNombre;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/pacientes")
public class PacienteController {

    @Autowired
    private PacienteService pacienteService;
    @Autowired
    private UsuarioService usuarioService;

    // Metodos front

    @GetMapping
    public String listarPacientes(Model model) {
        // Añade la lista de pacientes al modelo para que la vista pueda usarla
        model.addAttribute("pacientes", pacienteService.listarTodosLosPacientes());
        return "gestion-pacientes"; // Devuelve "gestion-pacientes.html"
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevoPaciente(Model model) {
        model.addAttribute("paciente", new PacienteDTO());
        // Buscar y añadir la lista de cuidadores y familiares al modelo
        model.addAttribute("cuidadores", usuarioService.findByRol(RolNombre.Cuidador));
        model.addAttribute("familiares", usuarioService.findByRol(RolNombre.Familiar));
        return "formulario-paciente";
    }

    @PostMapping("/crear")
    public String crearPaciente(PacienteDTO pacienteDTO,
                                @RequestParam("cuidadorId") Long cuidadorId,
                                @RequestParam(value = "familiarId", required = false) Long familiarId,
                                Authentication authentication) {

        // Obtenemos el ID del admin que está realizando la operación
        // (Esto requiere que el método findByEmail devuelva un UsuarioDTO con ID)
        Long adminId = usuarioService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Admin no encontrado")).getIdUsuario();

        // Llamamos a un nuevo método en el servicio que hará todo el trabajo
        pacienteService.crearPacienteYAsignar(pacienteDTO, cuidadorId, familiarId, adminId);

        return "redirect:/pacientes";
    }


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