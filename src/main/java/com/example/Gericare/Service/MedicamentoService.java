package com.example.Gericare.Service;

import com.example.Gericare.DTO.MedicamentoDTO;
import java.util.List;
import java.util.Map;
import java.util.Optional;
// Carga masiva datos
import java.io.InputStream;

public interface MedicamentoService {

    // Lista todos los medicamentos activos.
    List<MedicamentoDTO> listarMedicamentosActivos();

    // Guarda un nuevo medicamento o actualiza uno existente.
    MedicamentoDTO guardarMedicamento(MedicamentoDTO dto);

    // Elimina un medicamento (lógica de desactivación).
    void eliminarMedicamento(Long id);

    // Obtiene un medicamento por su ID.
    Optional<MedicamentoDTO> obtenerMedicamentoPorId(Long id);

    // Carga medicamentos desde un Excel
    Map<String, Integer> cargarDesdeExcel(InputStream inputStream) throws Exception;
}