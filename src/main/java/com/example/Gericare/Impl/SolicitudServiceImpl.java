package com.example.Gericare.Impl;

import com.example.Gericare.DTO.SolicitudDTO;
import com.example.Gericare.Entity.Administrador;
import com.example.Gericare.Entity.Familiar;
import com.example.Gericare.Entity.Paciente;
import com.example.Gericare.Entity.Solicitud;
import com.example.Gericare.Enums.EstadoSolicitud;
import com.example.Gericare.Enums.RolNombre;
import com.example.Gericare.Repository.PacienteRepository;
import com.example.Gericare.Repository.SolicitudRepository;
import com.example.Gericare.Repository.UsuarioRepository;
import com.example.Gericare.Service.SolicitudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SolicitudServiceImpl implements SolicitudService {

    @Autowired
    private SolicitudRepository solicitudRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public SolicitudDTO crearSolicitud(SolicitudDTO solicitudDTO, Long familiarId) {
        Paciente paciente = pacienteRepository.findById(solicitudDTO.getPacienteId())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado con ID: " + solicitudDTO.getPacienteId()));

        Familiar familiar = (Familiar) usuarioRepository.findById(familiarId)
                .filter(u -> u instanceof Familiar)
                .orElseThrow(() -> new RuntimeException("Familiar no encontrado con ID: " + familiarId));

        Solicitud nuevaSolicitud = new Solicitud();
        nuevaSolicitud.setPaciente(paciente);
        nuevaSolicitud.setFamiliar(familiar);
        nuevaSolicitud.setTipoSolicitud(solicitudDTO.getTipoSolicitud());
        nuevaSolicitud.setDetalleOtro(solicitudDTO.getDetalleOtro());
        nuevaSolicitud.setMotivoSolicitud(solicitudDTO.getMotivoSolicitud());
        nuevaSolicitud.setFechaSolicitud(LocalDateTime.now());
        nuevaSolicitud.setEstadoSolicitud(EstadoSolicitud.Pendiente); // Estado por defecto

        Solicitud solicitudGuardada = solicitudRepository.save(nuevaSolicitud);
        return toDTO(solicitudGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SolicitudDTO> obtenerSolicitudPorId(Long id) {
        return solicitudRepository.findById(id).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudDTO> listarTodasSolicitudesActivas() {
        return solicitudRepository.findByEstadoSolicitudNot(EstadoSolicitud.Inactivo)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudDTO> listarSolicitudesActivasPorFamiliar(Long familiarId) {
        return solicitudRepository.findByFamiliarIdUsuarioAndEstadoSolicitudNot(familiarId, EstadoSolicitud.Inactivo)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Optional<SolicitudDTO> aprobarSolicitud(Long id, Long adminId) {
        Administrador admin = (Administrador) usuarioRepository.findById(adminId)
                .filter(u -> u instanceof Administrador)
                .orElseThrow(() -> new RuntimeException("Administrador no encontrado con ID: " + adminId));

        return solicitudRepository.findById(id).map(solicitud -> {
            if (solicitud.getEstadoSolicitud() != EstadoSolicitud.Pendiente) {
                throw new IllegalStateException("Solo se pueden aprobar solicitudes pendientes.");
            }
            solicitud.setEstadoSolicitud(EstadoSolicitud.Aprobada);
            solicitud.setAdministrador(admin); // Registrar quién gestionó
            return toDTO(solicitudRepository.save(solicitud));
        });
    }

    @Override
    @Transactional
    public Optional<SolicitudDTO> rechazarSolicitud(Long id, Long adminId) {
        Administrador admin = (Administrador) usuarioRepository.findById(adminId)
                .filter(u -> u instanceof Administrador)
                .orElseThrow(() -> new RuntimeException("Administrador no encontrado con ID: " + adminId));

        return solicitudRepository.findById(id).map(solicitud -> {
            if (solicitud.getEstadoSolicitud() != EstadoSolicitud.Pendiente) {
                throw new IllegalStateException("Solo se pueden rechazar solicitudes pendientes.");
            }
            solicitud.setEstadoSolicitud(EstadoSolicitud.Rechazada);
            solicitud.setAdministrador(admin); // Registrar quién gestionó
            return toDTO(solicitudRepository.save(solicitud));
        });
    }

    @Override
    @Transactional
    public SolicitudDTO actualizarSolicitud(Long id, SolicitudDTO solicitudDTO, Long familiarId)
            throws AccessDeniedException, IllegalStateException {

        // Buscar la solicitud existente
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Solicitud no encontrada con ID: " + id));

        // Verificar que el usuario es el dueño
        if (!solicitud.getFamiliar().getIdUsuario().equals(familiarId)) {
            throw new AccessDeniedException("No tiene permisos para modificar esta solicitud.");
        }

        // Verificar que la solicitud sigue Pendiente
        if (solicitud.getEstadoSolicitud() != EstadoSolicitud.Pendiente) {
            throw new IllegalStateException("La solicitud ya no puede ser modificada porque fue procesada.");
        }

        // Buscar el paciente (si cambió)
        Paciente paciente = pacienteRepository.findById(solicitudDTO.getPacienteId())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado con ID: " + solicitudDTO.getPacienteId()));

        // Actualizar campos permitidos
        solicitud.setPaciente(paciente);
        solicitud.setTipoSolicitud(solicitudDTO.getTipoSolicitud());
        solicitud.setDetalleOtro(solicitudDTO.getDetalleOtro());
        solicitud.setMotivoSolicitud(solicitudDTO.getMotivoSolicitud());
        // No se actualiza la fecha, ni el estado, ni el familiar

        Solicitud solicitudActualizada = solicitudRepository.save(solicitud);
        return toDTO(solicitudActualizada);
    }

    @Override
    @Transactional
    public void eliminarSolicitudLogico(Long id, Long usuarioId, String rolUsuario) {
        // Buscar solicitud
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("La solicitud que intenta eliminar no fue encontrada o ya no existe."));

            boolean isAdmin = rolUsuario.equals(RolNombre.Administrador.name());
            boolean isFamiliarOwner = rolUsuario.equals(RolNombre.Familiar.name()) && solicitud.getFamiliar().getIdUsuario().equals(usuarioId);

            if (isAdmin) {
                if (solicitud.getEstadoSolicitud() == EstadoSolicitud.Pendiente) {
                    throw new IllegalStateException("El administrador no puede eliminar una solicitud pendiente. Debe aprobarla o rechazarla primero.");
                }
                // Admin puede eliminar aprovada o rechazada
                solicitud.setEstadoSolicitud(EstadoSolicitud.Inactivo);
                solicitudRepository.save(solicitud);
            } else if (isFamiliarOwner) {
                if (solicitud.getEstadoSolicitud() != EstadoSolicitud.Pendiente) {
                    throw new AccessDeniedException("Solo puedes eliminar tus solicitudes si están pendientes.");
                }
                // Familiar puede eliminar pendiente
                solicitud.setEstadoSolicitud(EstadoSolicitud.Inactivo);
                solicitudRepository.save(solicitud);
            } else {
                throw new AccessDeniedException("No tienes permiso para eliminar esta solicitud.");
            }
    }

    @Override
    @Transactional
    public void desactivarSolicitudesPorPaciente(Long pacienteId) {
        List<Solicitud> solicitudes = solicitudRepository.findByPacienteIdPaciente(pacienteId);
        solicitudes.forEach(solicitud -> {
            if (solicitud.getEstadoSolicitud() != EstadoSolicitud.Inactivo) {
                solicitud.setEstadoSolicitud(EstadoSolicitud.Inactivo);
            }
        });
        solicitudRepository.saveAll(solicitudes);
    }


    // A DTO
    private SolicitudDTO toDTO(Solicitud solicitud) {
        String pacienteNombre = solicitud.getPaciente() != null ? solicitud.getPaciente().getNombre() + " " + solicitud.getPaciente().getApellido() : null;
        String familiarNombre = solicitud.getFamiliar() != null ? solicitud.getFamiliar().getNombre() + " " + solicitud.getFamiliar().getApellido() : null;
        String adminNombre = solicitud.getAdministrador() != null ? solicitud.getAdministrador().getNombre() + " " + solicitud.getAdministrador().getApellido() : null;
        Long adminId = solicitud.getAdministrador() != null ? solicitud.getAdministrador().getIdUsuario() : null;

        return new SolicitudDTO(
                solicitud.getIdSolicitud(),
                solicitud.getPaciente() != null ? solicitud.getPaciente().getIdPaciente() : null,
                pacienteNombre,
                solicitud.getFamiliar() != null ? solicitud.getFamiliar().getIdUsuario() : null,
                familiarNombre,
                adminId,
                adminNombre,
                solicitud.getTipoSolicitud(),
                solicitud.getDetalleOtro(),
                solicitud.getFechaSolicitud(),
                solicitud.getMotivoSolicitud(),
                solicitud.getEstadoSolicitud()
        );
    }
}