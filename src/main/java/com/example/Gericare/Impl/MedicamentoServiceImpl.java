package com.example.Gericare.Impl;

import com.example.Gericare.DTO.MedicamentoDTO;
import com.example.Gericare.Entity.Medicamento;
import com.example.Gericare.Enums.EstadoUsuario;
import com.example.Gericare.Repository.MedicamentoRepository;
import com.example.Gericare.Service.MedicamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MedicamentoServiceImpl implements MedicamentoService {

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MedicamentoDTO> listarMedicamentosActivos() {
        // Filtrar solo los activos
        return medicamentoRepository.findAll().stream()
                .filter(med -> med.getEstado() == EstadoUsuario.Activo)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MedicamentoDTO guardarMedicamento(MedicamentoDTO dto) {
        // Validación básica de nombre
        if (dto.getNombreMedicamento() == null || dto.getNombreMedicamento().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del medicamento no puede estar vacío.");
        }

        Medicamento med;
        if (dto.getIdMedicamento() != null) {
            // Actualización
            med = medicamentoRepository.findById(dto.getIdMedicamento())
                    .orElseThrow(() -> new RuntimeException("Medicamento no encontrado con id: " + dto.getIdMedicamento()));
            // Opcional: Validar si el nombre está siendo cambiado a uno que ya existe
        } else {
            // Creación
            // Opcional: Validar si ya existe un medicamento con ese nombre
            med = new Medicamento();
        }
        med.setNombreMedicamento(dto.getNombreMedicamento().trim()); // Quitar espacios extra
        med.setDescripcionMedicamento(dto.getDescripcionMedicamento() != null ? dto.getDescripcionMedicamento().trim() : null);
        med.setEstado(EstadoUsuario.Activo); // Asegurar estado activo

        try {
            Medicamento medGuardado = medicamentoRepository.save(med);
            return mapToDTO(medGuardado);
        } catch (DataIntegrityViolationException e) {
            // Capturar violaciones de integridad, como nombres duplicados
            throw new RuntimeException("Ya existe un medicamento con el nombre: " + dto.getNombreMedicamento(), e);
        }
    }

    @Override
    @Transactional
    public void eliminarMedicamento(Long id) {
        medicamentoRepository.findById(id).ifPresent(med -> {
            if (med.getEstado() == EstadoUsuario.Activo) { // Solo inactivar si está activo
                med.setEstado(EstadoUsuario.Inactivo);
                medicamentoRepository.save(med);
                // Opcional: Manejar relaciones en otras tablas como historias clínicas
            }
        });
        // Si no se encuentra, simplemente no hacer nada (o lanzar excepción si se prefiere)
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MedicamentoDTO> obtenerMedicamentoPorId(Long id) {
        return medicamentoRepository.findById(id)
                // Solo devolver si está activo
                .filter(med -> med.getEstado() == EstadoUsuario.Activo)
                .map(this::mapToDTO);
    }

    // Mapeador privado
    private MedicamentoDTO mapToDTO(Medicamento med) {
        if (med == null) return null;
        return new MedicamentoDTO(med.getIdMedicamento(), med.getNombreMedicamento(), med.getDescripcionMedicamento());
    }
}