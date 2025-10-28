package com.example.Gericare.Controller;

import com.example.Gericare.DTO.PacienteDTO;
import com.example.Gericare.Repository.PacienteAsignadoRepository;
import com.example.Gericare.Service.PacienteService;
import com.example.Gericare.Service.UsuarioService;
import com.example.Gericare.Enums.EstadoAsignacion;
import com.example.Gericare.Enums.RolNombre;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/pacientes")
public class PacienteController {

    @Autowired
    private PacienteService pacienteService;
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private PacienteAsignadoRepository pacienteAsignadoRepository;

    @GetMapping
    public String listarPacientes(Model model,
                                  @RequestParam(required = false) String nombre,
                                  @RequestParam(required = false) String documento) {
        List<PacienteDTO> pacientes = pacienteService.listarPacientesFiltrados(nombre, documento);

        Map<Long, String> nombresFamiliares = pacientes.stream()
                .collect(Collectors.toMap(
                        PacienteDTO::getIdPaciente,
                        paciente -> pacienteAsignadoRepository.findByPacienteIdPacienteAndEstado(paciente.getIdPaciente(), EstadoAsignacion.Activo)
                                .stream()
                                .findFirst()
                                .map(asignacion -> asignacion.getFamiliar() != null ? asignacion.getFamiliar().getNombre() + " " + asignacion.getFamiliar().getApellido() : "N/A")
                                .orElse("N/A")
                ));

        model.addAttribute("pacientes", pacientes);
        model.addAttribute("nombresFamiliares", nombresFamiliares);
        return "paciente/admin-gestion-pacientes";
    }

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

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevoPaciente(Model model) {
        model.addAttribute("paciente", new PacienteDTO());
        model.addAttribute("cuidadores", usuarioService.findUsuariosByCriteria(null, null, RolNombre.Cuidador, null));
        model.addAttribute("familiares", usuarioService.findUsuariosByCriteria(null, null, RolNombre.Familiar, null));
        return "paciente/admin-formulario-paciente";
    }

    @PostMapping("/crear")
    public String crearPaciente(@Valid @ModelAttribute("paciente") PacienteDTO pacienteDTO,
                                BindingResult bindingResult,
                                @RequestParam("cuidadorId") Long cuidadorId,
                                @RequestParam(value = "familiarId", required = false) Long familiarId,
                                Authentication authentication,
                                Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("cuidadores", usuarioService.findUsuariosByCriteria(null, null, RolNombre.Cuidador, null));
            model.addAttribute("familiares", usuarioService.findUsuariosByCriteria(null, null, RolNombre.Familiar, null));
            return "paciente/admin-formulario-paciente";
        }
        try {
            Long adminId = usuarioService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Admin no encontrado")).getIdUsuario();
            pacienteService.crearPacienteYAsignar(pacienteDTO, cuidadorId, familiarId, adminId);
            redirectAttributes.addFlashAttribute("successMessage", "¡Paciente creado con éxito!");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ya existe un paciente con el mismo documento.");
            redirectAttributes.addFlashAttribute("paciente", pacienteDTO);
            return "redirect:/pacientes/nuevo";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al crear el paciente: " + e.getMessage());
            redirectAttributes.addFlashAttribute("paciente", pacienteDTO);
            return "redirect:/pacientes/nuevo";
        }
        return "redirect:/pacientes";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditarPaciente(@PathVariable Long id, Model model) {
        pacienteService.obtenerPacientePorId(id).ifPresent(paciente -> {
            model.addAttribute("paciente", paciente);
            model.addAttribute("cuidadores", usuarioService.findUsuariosByCriteria(null, null, RolNombre.Cuidador, null));
            model.addAttribute("familiares", usuarioService.findUsuariosByCriteria(null, null, RolNombre.Familiar, null));

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
        return "paciente/admin-formulario-paciente-editar";
    }

    @PostMapping("/editar/{id}")
    public String actualizarPaciente(@PathVariable Long id,
                                     @ModelAttribute("paciente") PacienteDTO pacienteDTO,
                                     @RequestParam("cuidadorId") Long cuidadorId,
                                     @RequestParam(value = "familiarId", required = false) Long familiarId,
                                     Authentication authentication, RedirectAttributes redirectAttributes) {
        Long adminId = usuarioService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Admin no encontrado")).getIdUsuario();
        pacienteService.actualizarPacienteYReasignar(id, pacienteDTO, cuidadorId, familiarId, adminId);
        redirectAttributes.addFlashAttribute("successMessage", "¡Paciente actualizado con éxito!");
        return "redirect:/pacientes";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarPaciente(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        pacienteService.eliminarPaciente(id);
        redirectAttributes.addFlashAttribute("successMessage", "¡Paciente eliminado con éxito!");
        return "redirect:/pacientes";
    }
}
