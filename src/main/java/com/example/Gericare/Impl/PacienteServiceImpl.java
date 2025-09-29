package com.example.Gericare.Impl;

import com.example.Gericare.DTO.PacienteDTO;
import com.example.Gericare.Repository.PacienteAsignadoRepository;
import com.example.Gericare.Repository.PacienteRepository;
import com.example.Gericare.Repository.UsuarioRepository;
import com.example.Gericare.Service.PacienteAsignadoService;
import com.example.Gericare.Service.PacienteService;
import com.example.Gericare.entity.Paciente;
import com.example.Gericare.entity.PacienteAsignado;
import com.example.Gericare.entity.Usuario;
import com.example.Gericare.enums.EstadoAsignacion;
import com.example.Gericare.enums.EstadoPaciente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PacienteServiceImpl implements PacienteService {

    @Autowired
    private PacienteRepository pacienteRepository;
    @Autowired
    private PacienteAsignadoService pacienteAsignadoService;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PacienteAsignadoRepository pacienteAsignadoRepository;

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
    @Transactional
    public void actualizarPacienteYReasignar(Long pacienteId, PacienteDTO pacienteDTO, Long cuidadorId, Long familiarId, Long adminId) {
        // 1. Busca al paciente o lanza un error si no existe.
        Paciente pacienteExistente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new RuntimeException("No se encontró el paciente con ID: " + pacienteId));

        // 2. Actualiza los campos básicos del paciente.
        pacienteExistente.setContactoEmergencia(pacienteDTO.getContactoEmergencia());
        pacienteExistente.setEstadoCivil(pacienteDTO.getEstadoCivil());
        pacienteExistente.setSeguroMedico(pacienteDTO.getSeguroMedico());
        pacienteExistente.setNumeroSeguro(pacienteDTO.getNumeroSeguro());

        // 3. Busca la asignación activa actual para compararla.
        PacienteAsignado asignacionActual = pacienteAsignadoRepository.findByPacienteIdPacienteAndEstado(pacienteId, EstadoAsignacion.Activo)
                .stream().findFirst().orElse(null);

        boolean haCambiadoLaAsignacion = false;
        if (asignacionActual == null) {
            // Si no hay ninguna asignación activa, se debe crear una.
            haCambiadoLaAsignacion = true;
        } else {
            // Comprueba si el cuidador o el familiar son diferentes.
            Long familiarActualId = (asignacionActual.getFamiliar() != null) ? asignacionActual.getFamiliar().getIdUsuario() : null;
            if (!asignacionActual.getCuidador().getIdUsuario().equals(cuidadorId) ||
                    !Objects.equals(familiarActualId, familiarId)) {
                haCambiadoLaAsignacion = true;
            }
        }

        // 4. Si la asignación ha cambiado, actualiza la referencia del familiar y crea la nueva asignación.
        if (haCambiadoLaAsignacion) {
            if (familiarId != null) {
                Usuario familiar = usuarioRepository.findById(familiarId)
                        .orElseThrow(() -> new RuntimeException("No se encontró el familiar con ID: " + familiarId));
                pacienteExistente.setUsuarioFamiliar(familiar);
            } else {
                pacienteExistente.setUsuarioFamiliar(null);
            }
            pacienteAsignadoService.crearAsignacion(pacienteId, cuidadorId, familiarId, adminId);
        }

        // Si no ha cambiado, @Transactional guardará los cambios del paciente (paso 2) automáticamente sin crear una nueva asignación.
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