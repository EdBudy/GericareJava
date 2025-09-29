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

    // metodos publicos de service

    @Override
    @Transactional
    public PacienteDTO crearPacienteYAsignar(PacienteDTO pacienteDTO, Long cuidadorId, Long familiarId, Long adminId) {
        Paciente nuevoPaciente = toEntity(pacienteDTO);
        nuevoPaciente.setEstado(EstadoPaciente.Activo);
        Paciente pacienteGuardado = pacienteRepository.save(nuevoPaciente);
        pacienteAsignadoService.crearAsignacion(pacienteGuardado.getIdPaciente(), cuidadorId, familiarId, adminId);
        return toDTO(pacienteGuardado);
    }

    @Override
    public PacienteDTO crearPaciente(PacienteDTO pacienteDTO) {
        Paciente nuevoPaciente = toEntity(pacienteDTO);
        nuevoPaciente.setEstado(EstadoPaciente.Activo);
        Paciente pacienteGuardado = pacienteRepository.save(nuevoPaciente);
        return toDTO(pacienteGuardado);
    }

    @Override
    public List<PacienteDTO> listarTodosLosPacientes() {
        return pacienteRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public Optional<PacienteDTO> obtenerPacientePorId(Long id) {
        return pacienteRepository.findById(id).map(this::toDTO);
    }

    @Override
    public Optional<PacienteDTO> actualizarPaciente(Long id, PacienteDTO pacienteDTO) {
        return pacienteRepository.findById(id).map(pacienteExistente -> {
            // Actualiza solo los campos que se pueden editar
            pacienteExistente.setContactoEmergencia(pacienteDTO.getContactoEmergencia());
            pacienteExistente.setEstadoCivil(pacienteDTO.getEstadoCivil());
            pacienteExistente.setSeguroMedico(pacienteDTO.getSeguroMedico());
            pacienteExistente.setNumeroSeguro(pacienteDTO.getNumeroSeguro());

            // Asigna los valores de los campos no editables para que no se pierdan
            pacienteExistente.setNombre(pacienteDTO.getNombre());
            pacienteExistente.setApellido(pacienteDTO.getApellido());
            pacienteExistente.setDocumentoIdentificacion(pacienteDTO.getDocumentoIdentificacion());
            pacienteExistente.setFechaNacimiento(pacienteDTO.getFechaNacimiento());
            pacienteExistente.setGenero(pacienteDTO.getGenero());
            pacienteExistente.setTipoSangre(pacienteDTO.getTipoSangre());


            Paciente pacienteActualizado = pacienteRepository.save(pacienteExistente);
            return toDTO(pacienteActualizado);
        });
    }

    @Override
    public void eliminarPaciente(Long id) {
        pacienteRepository.findById(id).ifPresent(paciente -> {
            paciente.setEstado(EstadoPaciente.Inactivo);
            pacienteRepository.save(paciente);
        });
    }

    // Metodos privados de conversion entre entidad y DTO

    private PacienteDTO toDTO(Paciente paciente) {
        // Comprobación para evitar un error si el familiar es nulo.
        String nombreFamiliar = null;
        if (paciente.getUsuarioFamiliar() != null) {
            nombreFamiliar = paciente.getUsuarioFamiliar().getNombre() + " "
                    + paciente.getUsuarioFamiliar().getApellido();
        }

        // Crea y retorna un DTO con TODOS los datos necesarios.
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
        // El ID no se asigna aquí porque se genera automáticamente.
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
        return paciente;
    }
}