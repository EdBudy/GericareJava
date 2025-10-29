package com.example.Gericare.Impl;

import com.example.Gericare.DTO.EnfermedadDTO;
import com.example.Gericare.Entity.Enfermedad;
import com.example.Gericare.Enums.EstadoUsuario;
import com.example.Gericare.Repository.EnfermedadRepository;
import com.example.Gericare.Service.EnfermedadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EnfermedadServiceImpl implements EnfermedadService {

    @Autowired
    private EnfermedadRepository enfermedadRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EnfermedadDTO> listarEnfermedadesActivas() {
        return enfermedadRepository.findAll().stream()
                .filter(enf -> enf.getEstado() == EstadoUsuario.Activo)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EnfermedadDTO guardarEnfermedad(EnfermedadDTO dto) {
        if (dto.getNombreEnfermedad() == null || dto.getNombreEnfermedad().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la enfermedad no puede estar vacío.");
        }

        Enfermedad enf;
        if (dto.getIdEnfermedad() != null) {
            enf = enfermedadRepository.findById(dto.getIdEnfermedad())
                    .orElseThrow(() -> new RuntimeException("Enfermedad no encontrada con id: " + dto.getIdEnfermedad()));
        } else {
            enf = new Enfermedad();
        }
        enf.setNombreEnfermedad(dto.getNombreEnfermedad().trim());
        enf.setDescripcionEnfermedad(dto.getDescripcionEnfermedad() != null ? dto.getDescripcionEnfermedad().trim() : null);
        enf.setEstado(EstadoUsuario.Activo);

        try {
            Enfermedad enfGuardada = enfermedadRepository.save(enf);
            return mapToDTO(enfGuardada);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Ya existe una enfermedad con el nombre: " + dto.getNombreEnfermedad(), e);
        }
    }

    @Override
    @Transactional
    public void eliminarEnfermedad(Long id) {
        enfermedadRepository.findById(id).ifPresent(enf -> {
            if (enf.getEstado() == EstadoUsuario.Activo) {
                enf.setEstado(EstadoUsuario.Inactivo);
                enfermedadRepository.save(enf);
            }
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EnfermedadDTO> obtenerEnfermedadPorId(Long id) {
        return enfermedadRepository.findById(id)
                .filter(enf -> enf.getEstado() == EstadoUsuario.Activo)
                .map(this::mapToDTO);
    }

    // Mapeador privado
    private EnfermedadDTO mapToDTO(Enfermedad enf) {
        if (enf == null) return null;
        return new EnfermedadDTO(enf.getIdEnfermedad(), enf.getNombreEnfermedad(), enf.getDescripcionEnfermedad());
    }
}