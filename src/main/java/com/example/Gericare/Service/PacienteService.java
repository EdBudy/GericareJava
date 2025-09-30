// En: src/main/java/com/example/Gericare/Service/PacienteService.java
package com.example.Gericare.Service;

import com.example.Gericare.DTO.PacienteDTO;
import java.util.List;
import java.util.Optional;
import java.io.IOException; // Asegúrate de tener esta importación
import java.io.OutputStream; // Y esta


public interface PacienteService {

    Optional<PacienteDTO> obtenerPacientePorId(Long id);
    void actualizarPacienteYReasignar(Long pacienteId, PacienteDTO pacienteDTO, Long cuidadorId, Long familiarId, Long adminId);
    PacienteDTO crearPacienteYAsignar(PacienteDTO pacienteDTO, Long cuidadorId, Long familiarId, Long adminId);
    void eliminarPaciente(Long id); // Borrado lógico
    List<PacienteDTO> listarPacientesFiltrados(String nombre, String documento);

    // Nuevos métodos para exportar la lista filtrada
    void exportarPacientesAExcel(OutputStream outputStream, String nombre, String documento) throws IOException;
    void exportarPacientesAPDF(OutputStream outputStream, String nombre, String documento) throws IOException;
}