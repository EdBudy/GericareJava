package com.example.Gericare.Impl;

import com.example.Gericare.DTO.ActividadDTO;
import com.example.Gericare.Repository.ActividadRepository;
import com.example.Gericare.Repository.PacienteAsignadoRepository;
import com.example.Gericare.Repository.PacienteRepository;
import com.example.Gericare.Repository.UsuarioRepository;
import com.example.Gericare.Service.ActividadService;
import com.example.Gericare.Entity.Actividad;
import com.example.Gericare.Entity.Administrador;
import com.example.Gericare.Entity.Paciente;
import com.example.Gericare.Enums.EstadoActividad;
import com.example.Gericare.Enums.EstadoAsignacion;
import com.example.Gericare.specification.ActividadSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.Gericare.Service.FestivosService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ActividadServiceImpl implements ActividadService {

    @Autowired
    private ActividadRepository actividadRepository;
    @Autowired
    private PacienteRepository pacienteRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PacienteAsignadoRepository pacienteAsignadoRepository;
    @Autowired
    private FestivosService festivosService;


    @Override
    @Transactional(readOnly = true) // Métodos de solo lectura
    public List<ActividadDTO> listarActividades(String nombrePaciente, String tipoActividad, EstadoActividad estado) {
        return actividadRepository.findAll(ActividadSpecification.findByCriteria(nombrePaciente, tipoActividad, estado))
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ActividadDTO crearActividad(ActividadDTO actividadDTO) {
        if (festivosService.esFestivo(actividadDTO.getFechaActividad())) {
            throw new IllegalArgumentException("No es posible programar actividades en días festivos.");
        }
        Paciente paciente = pacienteRepository.findById(actividadDTO.getIdPaciente())
                .orElseThrow(() -> new RuntimeException("Error: Paciente no encontrado con id " + actividadDTO.getIdPaciente()));

        Administrador admin = (Administrador) usuarioRepository.findById(actividadDTO.getIdAdmin())
                .filter(u -> u instanceof Administrador)
                .orElseThrow(() -> new RuntimeException("Error: Administrador no encontrado con id " + actividadDTO.getIdAdmin()));

        Actividad actividad = new Actividad();
        actividad.setPaciente(paciente);
        actividad.setAdministrador(admin);
        actividad.setTipoActividad(actividadDTO.getTipoActividad());
        actividad.setDescripcionActividad(actividadDTO.getDescripcionActividad());
        actividad.setFechaActividad(actividadDTO.getFechaActividad());
        actividad.setHoraInicio(actividadDTO.getHoraInicio());
        actividad.setHoraFin(actividadDTO.getHoraFin());
        actividad.setEstadoActividad(EstadoActividad.Pendiente);

        Actividad nuevaActividad = actividadRepository.save(actividad);
        return toDTO(nuevaActividad);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ActividadDTO> obtenerActividadPorId(Long id) {
        return actividadRepository.findById(id).map(this::toDTO);
    }

    @Override
    @Transactional
    public Optional<ActividadDTO> actualizarActividad(Long id, ActividadDTO actividadDTO) {
        return actividadRepository.findById(id)
                .map(actividadExistente -> {
                    actividadExistente.setTipoActividad(actividadDTO.getTipoActividad());
                    actividadExistente.setDescripcionActividad(actividadDTO.getDescripcionActividad());
                    actividadExistente.setFechaActividad(actividadDTO.getFechaActividad());
                    actividadExistente.setHoraInicio(actividadDTO.getHoraInicio());
                    actividadExistente.setHoraFin(actividadDTO.getHoraFin());

                    Actividad actividadActualizada = actividadRepository.save(actividadExistente);
                    return toDTO(actividadActualizada);
                });
    }

    @Override
    @Transactional
    public void eliminarActividad(Long id) {
        actividadRepository.findById(id).ifPresent(actividad -> {
            actividad.setEstadoActividad(EstadoActividad.Inactivo);
            actividadRepository.save(actividad);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActividadDTO> listarActividadesPorCuidador(Long cuidadorId) {
        return actividadRepository.findActividadesByCuidador(cuidadorId);
    }

    @Override
    @Transactional
    public void marcarComoCompletada(Long actividadId, Long cuidadorId) {
        Actividad actividad = actividadRepository.findById(actividadId)
                .orElseThrow(() -> new RuntimeException("Actividad no encontrada con id: " + actividadId));

        // Validación de seguridad
        boolean esAsignado = pacienteAsignadoRepository.findByCuidador_idUsuarioAndPaciente_idPacienteAndEstado(
                        cuidadorId, actividad.getPaciente().getIdPaciente(), EstadoAsignacion.Activo)
                .isPresent();

        if (!esAsignado) {
            throw new AccessDeniedException("El cuidador no tiene permiso para modificar esta actividad.");
        }

        actividad.setEstadoActividad(EstadoActividad.Completada);
        actividadRepository.save(actividad);
    }

    private ActividadDTO toDTO(Actividad actividad) {
        // Lógica para obtener nombres
        String nombrePaciente = (actividad.getPaciente() != null)
                ? actividad.getPaciente().getNombre() + " " + actividad.getPaciente().getApellido() : "N/A";

        String nombreAdmin = (actividad.getAdministrador() != null)
                ? actividad.getAdministrador().getNombre() + " " + actividad.getAdministrador().getApellido() : "N/A";

        Long idPaciente = (actividad.getPaciente() != null) ? actividad.getPaciente().getIdPaciente() : null;
        Long idAdmin = (actividad.getAdministrador() != null) ? actividad.getAdministrador().getIdUsuario() : null;

        return new ActividadDTO(
                actividad.getIdActividad(),
                idPaciente,
                nombrePaciente,
                idAdmin,
                nombreAdmin,
                actividad.getTipoActividad(),
                actividad.getDescripcionActividad(),
                actividad.getFechaActividad(),
                actividad.getHoraInicio(),
                actividad.getHoraFin(),
                actividad.getEstadoActividad()
        );
    }
}

