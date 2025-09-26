package com.example.Gericare.Controller;

import com.example.Gericare.DTO.PacienteDTO;
import com.example.Gericare.Service.PacienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pacientes")
public class PacienteController {

    @Autowired
    private PacienteService pacienteService;

    // Get

    // Todos los pacientes
    @GetMapping
    public List<PacienteDTO> listarTodosLosPacientes() {
        return pacienteService.listarTodosLosPacientes();
    }

    // Por ID
    @GetMapping("/{id}")
    public ResponseEntity<PacienteDTO> obtenerPacientePorId(@PathVariable Long id) {
        return pacienteService.obtenerPacientePorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Post

    // Nuevo paciente
    @PostMapping
    public PacienteDTO crearPaciente(@RequestBody PacienteDTO pacienteDTO) {
        return pacienteService.crearPaciente(pacienteDTO);
    }

    // Put

    // Actualizar paciente existente
    @PutMapping("/{id}")
    public ResponseEntity<PacienteDTO> actualizarPaciente(@PathVariable Long id, @RequestBody PacienteDTO pacienteDTO) {
        return pacienteService.actualizarPaciente(id, pacienteDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Delete

    // (borrado lógico)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPaciente(@PathVariable Long id) {
        if (pacienteService.obtenerPacientePorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        pacienteService.eliminarPaciente(id);
        return ResponseEntity.noContent().build();
    }
}