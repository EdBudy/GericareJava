package com.example.Gericare.Controller;

import com.example.Gericare.DTO.PacienteDTO;
import com.example.Gericare.Repository.PacienteAsignadoRepository;
import com.example.Gericare.Service.PacienteService;
import com.example.Gericare.Service.UsuarioService;
import com.example.Gericare.enums.EstadoAsignacion;
import com.example.Gericare.enums.RolNombre;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/pacientes")
public class PacienteController {

    @Autowired
    private PacienteService pacienteService;
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private PacienteAsignadoRepository pacienteAsignadoRepository;

    // lista pacientes
    @GetMapping
    public String listarPacientes(Model model) {
        model.addAttribute("pacientes", pacienteService.listarTodosLosPacientes());
        return "gestion-pacientes";
    }

    // formulario crear nuevo paciente
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevoPaciente(Model model) {
        model.addAttribute("paciente", new PacienteDTO());
        model.addAttribute("cuidadores", usuarioService.findByRol(RolNombre.Cuidador));
        model.addAttribute("familiares", usuarioService.findByRol(RolNombre.Familiar));
        return "formulario-paciente";
    }

    // procesa la creacion de un nuevo paciente y su asignacion
    @PostMapping("/crear")
    public String crearPaciente(PacienteDTO pacienteDTO,
                                @RequestParam("cuidadorId") Long cuidadorId,
                                @RequestParam(value = "familiarId", required = false) Long familiarId,
                                Authentication authentication) {
        Long adminId = usuarioService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Admin no encontrado")).getIdUsuario();
        pacienteService.crearPacienteYAsignar(pacienteDTO, cuidadorId, familiarId, adminId);
        return "redirect:/pacientes";
    }

    // editar paciente
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditarPaciente(@PathVariable Long id, Model model) {
        pacienteService.obtenerPacientePorId(id).ifPresent(paciente -> {
            model.addAttribute("paciente", paciente);
            model.addAttribute("cuidadores", usuarioService.findByRol(RolNombre.Cuidador));
            model.addAttribute("familiares", usuarioService.findByRol(RolNombre.Familiar));

            // USAREMOS EL NUEVO MÉTODO CORRECTO DEL REPOSITORIO
            pacienteAsignadoRepository.findByPacienteIdPacienteAndEstado(id, EstadoAsignacion.Activo)
                    .stream()
                    .findFirst()
                    .ifPresent(asignacion -> {
                        model.addAttribute("cuidadorActualId", asignacion.getCuidador().getIdUsuario());
                        if (asignacion.getFamiliar() != null) {
                            model.addAttribute("familiarActualId", asignacion.getFamiliar().getIdUsuario());
                        }
                    });
        });
        return "formulario-paciente-editar";
    }

    @PostMapping("/editar/{id}")
    public String actualizarPaciente(@PathVariable Long id,
                                     @ModelAttribute("paciente") PacienteDTO pacienteDTO,
                                     @RequestParam("cuidadorId") Long cuidadorId,
                                     @RequestParam(value = "familiarId", required = false) Long familiarId,
                                     Authentication authentication) {
        // Obtenemos el ID del admin que está realizando el cambio
        Long adminId = usuarioService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Admin no encontrado")).getIdUsuario();

        pacienteService.actualizarPacienteYReasignar(id, pacienteDTO, cuidadorId, familiarId, adminId);

        return "redirect:/pacientes";
    }

    // eliminar paciente
    @PostMapping("/eliminar/{id}")
    public String eliminarPaciente(@PathVariable Long id) {
        pacienteService.eliminarPaciente(id);
        return "redirect:/pacientes";
    }
}