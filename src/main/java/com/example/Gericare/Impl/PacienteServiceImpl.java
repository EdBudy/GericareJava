package com.example.Gericare.Impl;

import com.example.Gericare.DTO.PacienteDTO;
import com.example.Gericare.Repository.PacienteRepository;
import com.example.Gericare.Service.PacienteAsignadoService;
import com.example.Gericare.Service.PacienteService;
import com.example.Gericare.entity.Paciente;
import com.example.Gericare.enums.EstadoPaciente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PacienteServiceImpl implements PacienteService {

    @Autowired
    private PacienteRepository pacienteRepository;
    @Autowired
    private PacienteAsignadoService pacienteAsignadoService;

    @Override
    public PacienteDTO crearPaciente(PacienteDTO pacienteDTO) {
        // Convertir el DTO recibido a una entidad para poder guardarla.
        Paciente nuevoPaciente = toEntity(pacienteDTO);
        // Asegurar de que el estado inicial sea 'Activo'.
        nuevoPaciente.setEstado(EstadoPaciente.Activo);
        // Guardar la entidad en la base de datos.
        Paciente pacienteGuardado = pacienteRepository.save(nuevoPaciente);
        // Convertir la entidad guardada de vuelta a un DTO para devolverla.
        return toDTO(pacienteGuardado);
    }

    @Override
    @Transactional
    public PacienteDTO crearPacienteYAsignar(PacienteDTO pacienteDTO, Long cuidadorId, Long familiarId, Long adminId) {
        // Crear paciente
        Paciente nuevoPaciente = toEntity(pacienteDTO);
        nuevoPaciente.setEstado(EstadoPaciente.Activo);
        Paciente pacienteGuardado = pacienteRepository.save(nuevoPaciente);

        // Usar ID del paciente recién creado para crear la asignación
        pacienteAsignadoService.crearAsignacion(
                pacienteGuardado.getIdPaciente(),
                cuidadorId,
                familiarId,
                adminId
        );

        return toDTO(pacienteGuardado);
    }

    @Override
    public List<PacienteDTO> listarTodosLosPacientes() {
        return pacienteRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PacienteDTO> obtenerPacientePorId(Long id) {
        return pacienteRepository.findById(id).map(this::toDTO);
    }

    @Override
    public Optional<PacienteDTO> actualizarPaciente(Long id, PacienteDTO pacienteDTO) {
        return pacienteRepository.findById(id).map(pacienteExistente -> {
            // Actualizar los campos del paciente existente con los del DTO.
            pacienteExistente.setNombre(pacienteDTO.getNombre());
            pacienteExistente.setApellido(pacienteDTO.getApellido());
            pacienteExistente.setFechaNacimiento(pacienteDTO.getFechaNacimiento());
            pacienteExistente.setGenero(pacienteDTO.getGenero());
            pacienteExistente.setContactoEmergencia(pacienteDTO.getContactoEmergencia());
            pacienteExistente.setEstadoCivil(pacienteDTO.getEstadoCivil());
            pacienteExistente.setTipoSangre(pacienteDTO.getTipoSangre());
            pacienteExistente.setSeguroMedico(pacienteDTO.getSeguroMedico());
            pacienteExistente.setNumeroSeguro(pacienteDTO.getNumeroSeguro());

            Paciente pacienteActualizado = pacienteRepository.save(pacienteExistente);
            return toDTO(pacienteActualizado);
        });
    }

    @Override
    public void eliminarPaciente(Long id) {
        // Borrado lógico.
        pacienteRepository.findById(id).ifPresent(paciente -> {
            paciente.setEstado(EstadoPaciente.Inactivo);
            pacienteRepository.save(paciente);
        });
    }

    private PacienteDTO toDTO(Paciente paciente) {
        String nombreFamiliar = null;
        if (paciente.getUsuarioFamiliar() != null) {
            nombreFamiliar = paciente.getUsuarioFamiliar().getNombre() + " "
                    + paciente.getUsuarioFamiliar().getApellido();
        }

        return new PacienteDTO(
                paciente.getIdPaciente(),
                paciente.getDocumentoIdentificacion(),
                paciente.getNombre(),
                paciente.getApellido(),
                paciente.getFechaNacimiento(),
                paciente.getGenero(),
                paciente.getContactoEmergencia(),
                paciente.getEstadoCivil(),
                paciente.getTipoSangre(),
                paciente.getSeguroMedico(),
                paciente.getNumeroSeguro(),
                paciente.getEstado(),
                nombreFamiliar);
    }

    private Paciente toEntity(PacienteDTO dto) {
        Paciente paciente = new Paciente();
        paciente.setDocumentoIdentificacion(dto.getDocumentoIdentificacion());
        paciente.setNombre(dto.getNombre());
        paciente.setApellido(dto.getApellido());
        paciente.setFechaNacimiento(dto.getFechaNacimiento());
        paciente.setGenero(dto.getGenero());
        paciente.setContactoEmergencia(dto.getContactoEmergencia());
        paciente.setEstadoCivil(dto.getEstadoCivil());
        paciente.setTipoSangre(dto.getTipoSangre());
        paciente.setSeguroMedico(dto.getSeguroMedico());
        paciente.setNumeroSeguro(dto.getNumeroSeguro());
        // El estado y el familiar asociado se manejan por separado en la lógica de
        // negocio.
        return paciente;
    }
}