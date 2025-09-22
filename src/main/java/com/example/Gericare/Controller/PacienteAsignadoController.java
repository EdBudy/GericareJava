package com.example.Gericare.Controller;

import com.example.Gericare.DTO.PacienteAsignadoDTO;
import com.example.Gericare.Service.PacienteAsignadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/asignaciones")
public class PacienteAsignadoController {

    @Autowired
    private PacienteAsignadoService pacienteAsignadoService;

    // Get

    @GetMapping
    public List<PacienteAsignadoDTO> listarTodasLasAsignaciones() {
        return pacienteAsignadoService.listarTodasLasAsignaciones();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PacienteAsignadoDTO> obtenerAsignacionPorId(@PathVariable Long id) {
        return pacienteAsignadoService.obtenerAsignacionPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Post

    // Los IDs se pasan como parámetros en la URL
    @PostMapping
    public PacienteAsignadoDTO crearAsignacion(@RequestParam Long pacienteId, @RequestParam Long cuidadorId, @RequestParam(required = false) Long familiarId, @RequestParam Long adminId) {
        return pacienteAsignadoService.crearAsignacion(pacienteId, cuidadorId, familiarId, adminId);
    }

    // Delete

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarAsignacion(@PathVariable Long id) {
        if (pacienteAsignadoService.obtenerAsignacionPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        pacienteAsignadoService.eliminarAsignacion(id);
        return ResponseEntity.noContent().build(); // Devolver 204 No Content para indicar éxito
    }
}