package com.example.Gericare.Impl;

import com.example.Gericare.DTO.TratamientoDTO;
import com.example.Gericare.Entity.*;
import com.example.Gericare.Enums.EstadoActividad;
import com.example.Gericare.Enums.EstadoAsignacion;
import com.example.Gericare.Repository.PacienteAsignadoRepository;
import com.example.Gericare.Repository.PacienteRepository;
import com.example.Gericare.Repository.TratamientoRepository;
import com.example.Gericare.Repository.UsuarioRepository;
import com.example.Gericare.Service.TratamientoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TratamientoServiceImpl implements TratamientoService {

    @Autowired
    private TratamientoRepository tratamientoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PacienteAsignadoRepository pacienteAsignadoRepository;

    @Override
    @Transactional
    public TratamientoDTO crearTratamiento(TratamientoDTO tratamientoDTO, Long adminId) {
        // Validar Paciente
        Paciente paciente = pacienteRepository.findById(tratamientoDTO.getPacienteId())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado con ID: " + tratamientoDTO.getPacienteId()));

        // Validar Administrador
        Administrador admin = (Administrador) usuarioRepository.findById(adminId)
                .filter(u -> u instanceof Administrador)
                .orElseThrow(() -> new RuntimeException("Administrador no encontrado con ID: " + adminId));

        // Buscar automaticamente al Cuidador asignado
        Cuidador cuidadorAsignado = pacienteAsignadoRepository
                .findByPacienteIdPacienteAndEstado(paciente.getIdPaciente(), EstadoAsignacion.Activo)
                .stream()
                .findFirst() // Debería haber solo uno activo
                .map(PacienteAsignado::getCuidador)
                .orElseThrow(() -> new IllegalStateException("El paciente seleccionado (ID: " + paciente.getIdPaciente() + ") no tiene un cuidador activo asignado actualmente. Asigne uno antes de crear tratamientos."));

        // Crear la entidad Tratamiento
        Tratamiento nuevoTratamiento = new Tratamiento();
        nuevoTratamiento.setPaciente(paciente);
        nuevoTratamiento.setAdministrador(admin);
        nuevoTratamiento.setCuidador(cuidadorAsignado); // Asignar el cuidador encontrado automáticamente
        nuevoTratamiento.setDescripcion(tratamientoDTO.getDescripcion());
        nuevoTratamiento.setInstruccionesEspeciales(tratamientoDTO.getInstruccionesEspeciales());
        nuevoTratamiento.setFechaInicio(tratamientoDTO.getFechaInicio());
        nuevoTratamiento.setFechaFin(tratamientoDTO.getFechaFin());
        nuevoTratamiento.setObservaciones(tratamientoDTO.getObservaciones());
        nuevoTratamiento.setEstadoTratamiento(EstadoActividad.Pendiente);

        // Guardar y devolver DTO
        Tratamiento tratamientoGuardado = tratamientoRepository.save(nuevoTratamiento);
        // El DTO incluirá la info del cuidador asignado para mostrarla si es necesario
        return toDTO(tratamientoGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TratamientoDTO> obtenerTratamientoPorId(Long id) {
        return tratamientoRepository.findById(id).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TratamientoDTO> listarTodosTratamientosActivos() {
        return tratamientoRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TratamientoDTO> listarTratamientosActivosPorCuidador(Long cuidadorId) {
        return tratamientoRepository.findTratamientosActivosByCuidadorAsignado(cuidadorId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TratamientoDTO> listarTratamientosActivosPorPaciente(Long pacienteId) {
        return tratamientoRepository.findByPacienteIdPaciente(pacienteId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Optional<TratamientoDTO> actualizarTratamientoAdmin(Long id, TratamientoDTO tratamientoDTO) {
        // Busca el tratamiento existente (@Where = no encontrará INACTIVOS)
        return tratamientoRepository.findById(id).map(tratamiento -> {

            // Validar que el paciente no cambie
            if (!tratamiento.getPaciente().getIdPaciente().equals(tratamientoDTO.getPacienteId())) {
                throw new IllegalArgumentException("No se puede cambiar el paciente de un tratamiento existente.");
            }

            // Busca automáticamente al cuidador asignado
            Cuidador cuidadorAsignado = pacienteAsignadoRepository
                    .findByPacienteIdPacienteAndEstado(tratamiento.getPaciente().getIdPaciente(), EstadoAsignacion.Activo)
                    .stream()
                    .findFirst()
                    .map(PacienteAsignado::getCuidador)
                    .orElseThrow(() -> new IllegalStateException("El paciente (ID: " + tratamiento.getPaciente().getIdPaciente() + ") no tiene un cuidador activo asignado actualmente."));

            // Actualizar campos
            tratamiento.setCuidador(cuidadorAsignado); // Asignación automática
            tratamiento.setDescripcion(tratamientoDTO.getDescripcion());
            tratamiento.setInstruccionesEspeciales(tratamientoDTO.getInstruccionesEspeciales());
            tratamiento.setFechaInicio(tratamientoDTO.getFechaInicio());
            tratamiento.setFechaFin(tratamientoDTO.getFechaFin());
            // Admin no actualiza observaciones ni estado (lo hace cuidador)

            return toDTO(tratamientoRepository.save(tratamiento));
        });
    }

    @Override
    @Transactional
    public Optional<TratamientoDTO> actualizarObservacionesCuidador(Long id, Long cuidadorId, String observaciones) {
        return tratamientoRepository.findById(id).map(tratamiento -> {
            // Verificar que el cuidador que edita es el asignado al tratamiento
            if (!tratamiento.getCuidador().getIdUsuario().equals(cuidadorId)) {
                throw new AccessDeniedException("No tienes permiso para editar las observaciones de este tratamiento.");
            }
            tratamiento.setObservaciones(observaciones);
            return toDTO(tratamientoRepository.save(tratamiento));
        });
    }

    @Override
    @Transactional
    public Optional<TratamientoDTO> completarTratamiento(Long id, Long cuidadorId) {
        return tratamientoRepository.findById(id).map(tratamiento -> {
            // Verificar que el cuidador que completa es el asignado al tratamiento
            if (!tratamiento.getCuidador().getIdUsuario().equals(cuidadorId)) {
                // Verificar si el cuidador es el asignado actualmente al paciente
                boolean esAsignadoActual = pacienteAsignadoRepository
                        .findByCuidador_idUsuarioAndPaciente_idPacienteAndEstado(
                                cuidadorId, tratamiento.getPaciente().getIdPaciente(), EstadoAsignacion.Activo)
                        .isPresent();
                if (!esAsignadoActual) {
                    throw new AccessDeniedException("No tienes permiso para completar este tratamiento.");
                }
            }

            if (tratamiento.getEstadoTratamiento() != EstadoActividad.Pendiente) {
                throw new IllegalStateException("Solo se pueden completar tratamientos pendientes.");
            }

            tratamiento.setEstadoTratamiento(EstadoActividad.Completada);
            // Si se completa el tratamiento, se adjunta la fecha fin
            if (tratamiento.getFechaFin() == null) {
                tratamiento.setFechaFin(LocalDate.now());
            }
            return toDTO(tratamientoRepository.save(tratamiento));
        });
    }

    @Override
    @Transactional
    public void eliminarTratamientoLogico(Long id) {
        tratamientoRepository.findById(id).ifPresent(tratamiento -> {
            tratamiento.setEstadoTratamiento(EstadoActividad.Inactivo);
            tratamientoRepository.save(tratamiento);
        });
    }

    @Override
    @Transactional
    public void desactivarTratamientosPorPaciente(Long pacienteId) {
        List<Tratamiento> tratamientos = tratamientoRepository.findByPacienteIdPaciente(pacienteId);
        tratamientos.forEach(tratamiento -> {
            if (tratamiento.getEstadoTratamiento() != EstadoActividad.Inactivo) {
                tratamiento.setEstadoTratamiento(EstadoActividad.Inactivo);
            }
        });
        tratamientoRepository.saveAll(tratamientos);
    }

    // A DTO
    private TratamientoDTO toDTO(Tratamiento tratamiento) {
        String pacienteNombre = tratamiento.getPaciente() != null ? tratamiento.getPaciente().getNombre() + " " + tratamiento.getPaciente().getApellido() : null;
        String adminNombre = tratamiento.getAdministrador() != null ? tratamiento.getAdministrador().getNombre() + " " + tratamiento.getAdministrador().getApellido() : null;
        String cuidadorNombre = tratamiento.getCuidador() != null ? tratamiento.getCuidador().getNombre() + " " + tratamiento.getCuidador().getApellido() : null;

        return new TratamientoDTO(
                tratamiento.getIdTratamiento(),
                tratamiento.getPaciente() != null ? tratamiento.getPaciente().getIdPaciente() : null,
                pacienteNombre,
                tratamiento.getAdministrador() != null ? tratamiento.getAdministrador().getIdUsuario() : null,
                adminNombre,
                tratamiento.getCuidador() != null ? tratamiento.getCuidador().getIdUsuario() : null,
                cuidadorNombre,
                tratamiento.getDescripcion(),
                tratamiento.getInstruccionesEspeciales(),
                tratamiento.getFechaInicio(),
                tratamiento.getFechaFin(),
                tratamiento.getObservaciones(),
                tratamiento.getEstadoTratamiento()
        );
    }
}