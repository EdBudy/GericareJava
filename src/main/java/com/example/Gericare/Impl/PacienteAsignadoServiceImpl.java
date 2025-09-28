package com.example.Gericare.Impl;

import com.example.Gericare.DTO.PacienteAsignadoDTO;
import com.example.Gericare.DTO.PacienteDTO;
import com.example.Gericare.DTO.UsuarioDTO;
import com.example.Gericare.Repository.PacienteAsignadoRepository;
import com.example.Gericare.Repository.PacienteRepository;
import com.example.Gericare.Repository.UsuarioRepository;
import com.example.Gericare.Service.PacienteAsignadoService;
import com.example.Gericare.entity.*;
import com.example.Gericare.enums.EstadoAsignacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importante para transacciones

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PacienteAsignadoServiceImpl implements PacienteAsignadoService {

    @Autowired
    private PacienteAsignadoRepository pacienteAsignadoRepository;
    @Autowired
    private PacienteRepository pacienteRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    @Transactional // Anotación para asegurar que todas las operaciones se completen o ninguna.
    public PacienteAsignadoDTO crearAsignacion(Long idPaciente, Long idCuidador, Long idFamiliar, Long idAdmin) {
        // --- 1. Buscar y validar las entidades ---
        Paciente paciente = pacienteRepository.findById(idPaciente)
                .orElseThrow(() -> new RuntimeException("Error: Paciente no encontrado con id " + idPaciente));

        Cuidador cuidador = (Cuidador) usuarioRepository.findById(idCuidador)
                .filter(u -> u instanceof Cuidador)
                .orElseThrow(() -> new RuntimeException("Error: Cuidador no encontrado con id " + idCuidador));

        Administrador admin = (Administrador) usuarioRepository.findById(idAdmin)
                .filter(u -> u instanceof Administrador)
                .orElseThrow(() -> new RuntimeException("Error: Administrador no encontrado con id " + idAdmin));

        Familiar familiar = null;
        if (idFamiliar != null) {
            familiar = (Familiar) usuarioRepository.findById(idFamiliar)
                    .filter(u -> u instanceof Familiar)
                    .orElseThrow(() -> new RuntimeException("Error: Familiar no encontrado con id " + idFamiliar));
        }

        // --- 2. Aplicar la lógica de negocio ---
        // Antes de crear una nueva, desactivar cualquier asignación activa que este
        // paciente ya tenga.
        desactivarAsignacionesAnteriores(paciente);

        // --- 3. Crear la nueva entidad ---
        PacienteAsignado nuevaAsignacion = new PacienteAsignado();
        nuevaAsignacion.setPaciente(paciente);
        nuevaAsignacion.setCuidador(cuidador);
        nuevaAsignacion.setFamiliar(familiar);
        nuevaAsignacion.setAdminCreador(admin);
        nuevaAsignacion.setEstado(EstadoAsignacion.Activo);
        nuevaAsignacion.setFechaCreacion(LocalDateTime.now());

        // --- 4. Persistir y devolver el DTO ---
        PacienteAsignado asignacionGuardada = pacienteAsignadoRepository.save(nuevaAsignacion);
        return toDTO(asignacionGuardada);
    }

    @Override
    public List<PacienteAsignadoDTO> listarTodasLasAsignaciones() {
        return pacienteAsignadoRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public Optional<PacienteAsignadoDTO> obtenerAsignacionPorId(Long idAsignacion) {
        return pacienteAsignadoRepository.findById(idAsignacion).map(this::toDTO);
    }

    @Override
    public void eliminarAsignacion(Long idAsignacion) {
        // Aplicar borrado lógico.
        pacienteAsignadoRepository.findById(idAsignacion).ifPresent(asignacion -> {
            asignacion.setEstado(EstadoAsignacion.Inactivo);
            pacienteAsignadoRepository.save(asignacion);
        });
    }

    // --- MÉTODOS PRIVADOS ---

    private void desactivarAsignacionesAnteriores(Paciente paciente) {
        // Buscar todas las asignaciones que actualmente estén activas para este
        // paciente.
        List<PacienteAsignado> asignacionesActivas = pacienteAsignadoRepository.findByPacienteAndEstado(paciente,
                EstadoAsignacion.Activo);

        // Iterar sobre ellas y marcarlas como inactivas.
        for (PacienteAsignado asignacion : asignacionesActivas) {
            asignacion.setEstado(EstadoAsignacion.Inactivo);
        }

        // Guardar todos los cambios en la base de datos.
        pacienteAsignadoRepository.saveAll(asignacionesActivas);
    }

    // Este es el método principal que convierte la asignación
    public PacienteAsignadoDTO toDTO(PacienteAsignado asignacion) {
        PacienteDTO pacienteDTO = toPacienteDTO(asignacion.getPaciente());

        UsuarioDTO cuidadorDTO = toUsuarioDTO(asignacion.getCuidador());

        UsuarioDTO familiarDTO = null;
        if (asignacion.getFamiliar() != null) {
            familiarDTO = toUsuarioDTO(asignacion.getFamiliar());
        }

        return new PacienteAsignadoDTO(
                asignacion.getIdAsignacion(),
                pacienteDTO,
                cuidadorDTO,
                familiarDTO,
                asignacion.getEstado(),
                asignacion.getFechaCreacion(),
                asignacion.getAdminCreador().getNombre() + " " + asignacion.getAdminCreador().getApellido());
    }

    private PacienteDTO toPacienteDTO(Paciente paciente) {
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

    private UsuarioDTO toUsuarioDTO(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();

        dto.setIdUsuario(usuario.getIdUsuario());
        dto.setDocumentoIdentificacion(usuario.getDocumentoIdentificacion());
        dto.setNombre(usuario.getNombre());
        dto.setApellido(usuario.getApellido());
        dto.setDireccion(usuario.getDireccion());
        dto.setCorreoElectronico(usuario.getCorreoElectronico());

        if (usuario instanceof Empleado) {
            Empleado empleado = (Empleado) usuario;
            dto.setTipoContrato(empleado.getTipoContrato());
            dto.setContactoEmergencia(empleado.getContactoEmergencia());
            dto.setFechaNacimiento(empleado.getFechaNacimiento());
        } else if (usuario instanceof Familiar) {
            Familiar familiar = (Familiar) usuario;
            dto.setParentesco(familiar.getParentesco());
        }

        return dto;
    }
}