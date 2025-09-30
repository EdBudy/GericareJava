package com.example.Gericare.Controller;

import com.example.Gericare.DTO.PacienteDTO;
import com.example.Gericare.Repository.PacienteAsignadoRepository;
import com.example.Gericare.Service.PacienteService;
import com.example.Gericare.Service.UsuarioService;
import com.example.Gericare.enums.EstadoAsignacion;
import com.example.Gericare.enums.RolNombre;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Controller
@RequestMapping("/pacientes")
public class PacienteController {

    @Autowired
    private PacienteService pacienteService;
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private PacienteAsignadoRepository pacienteAsignadoRepository;

    // Muestra la lista de pacientes, ahora con filtros
    @GetMapping
    public String listarPacientes(Model model,
                                  @RequestParam(required = false) String nombre,
                                  @RequestParam(required = false) String documento) {
        model.addAttribute("pacientes", pacienteService.listarPacientesFiltrados(nombre, documento));
        return "gestion-pacientes";
    }

    // --- Endpoints de Exportación ---
    @GetMapping("/exportExcel")
    public ResponseEntity<InputStreamResource> exportarExcel(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String documento) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        pacienteService.exportarPacientesAExcel(outputStream, nombre, documento);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "pacientes.xlsx");
        return new ResponseEntity<>(new InputStreamResource(new ByteArrayInputStream(outputStream.toByteArray())), headers, HttpStatus.OK);
    }

    @GetMapping("/exportPdf")
    public ResponseEntity<InputStreamResource> exportarPdf(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String documento) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        pacienteService.exportarPacientesAPDF(outputStream, nombre, documento);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "pacientes.pdf");
        return new ResponseEntity<>(new InputStreamResource(new ByteArrayInputStream(outputStream.toByteArray())), headers, HttpStatus.OK);
    }

    // --- Flujo de Creación de Paciente ---
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevoPaciente(Model model) {
        model.addAttribute("paciente", new PacienteDTO());
        model.addAttribute("cuidadores", usuarioService.findUsuariosByCriteria(null, null, RolNombre.Cuidador, null));
        model.addAttribute("familiares", usuarioService.findUsuariosByCriteria(null, null, RolNombre.Familiar, null));
        return "formulario-paciente";
    }

    @PostMapping("/crear")
    public String crearPaciente(@Valid @ModelAttribute("paciente") PacienteDTO pacienteDTO,
                                BindingResult bindingResult,
                                @RequestParam("cuidadorId") Long cuidadorId,
                                @RequestParam(value = "familiarId", required = false) Long familiarId,
                                Authentication authentication,
                                Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("cuidadores", usuarioService.findUsuariosByCriteria(null, null, RolNombre.Cuidador, null));
            model.addAttribute("familiares", usuarioService.findUsuariosByCriteria(null, null, RolNombre.Familiar, null));
            return "formulario-paciente";
        }
        Long adminId = usuarioService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Admin no encontrado")).getIdUsuario();
        pacienteService.crearPacienteYAsignar(pacienteDTO, cuidadorId, familiarId, adminId);
        return "redirect:/pacientes";
    }

    // --- Flujo de Edición de Paciente (CORREGIDO) ---
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditarPaciente(@PathVariable Long id, Model model) {
        // Busca al paciente por su ID
        pacienteService.obtenerPacientePorId(id).ifPresent(paciente -> {
            model.addAttribute("paciente", paciente);
            // CORRECCIÓN 1: Usa el método correcto para obtener la lista de usuarios por rol
            model.addAttribute("cuidadores", usuarioService.findUsuariosByCriteria(null, null, RolNombre.Cuidador, null));
            model.addAttribute("familiares", usuarioService.findUsuariosByCriteria(null, null, RolNombre.Familiar, null));

        });
        return "formulario-paciente-editar"; // Asegúrate de que esta vista exista
    }

    @PostMapping("/editar/{id}")
    public String actualizarPaciente(@PathVariable Long id,
                                     @ModelAttribute("paciente") PacienteDTO pacienteDTO,
                                     @RequestParam("cuidadorId") Long cuidadorId,
                                     @RequestParam(value = "familiarId", required = false) Long familiarId,
                                     Authentication authentication) {
        Long adminId = usuarioService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Admin no encontrado")).getIdUsuario();
        pacienteService.actualizarPacienteYReasignar(id, pacienteDTO, cuidadorId, familiarId, adminId);
        return "redirect:/pacientes";
    }

    // --- Flujo de Eliminación ---
    @PostMapping("/eliminar/{id}")
    public String eliminarPaciente(@PathVariable Long id) {
        pacienteService.eliminarPaciente(id);
        return "redirect:/pacientes";
    }
}