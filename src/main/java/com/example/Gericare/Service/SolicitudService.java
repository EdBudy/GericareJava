package com.example.Gericare.Service;

import com.example.Gericare.DTO.SolicitudDTO;
import org.springframework.security.access.AccessDeniedException;
import java.util.List;
import java.util.Optional;

public interface SolicitudService {

    SolicitudDTO crearSolicitud(SolicitudDTO solicitudDTO, Long familiarId);

    Optional<SolicitudDTO> obtenerSolicitudPorId(Long id);

    // Listar solicitudes activas
    List<SolicitudDTO> listarTodasSolicitudesActivas(); // Admin

    List<SolicitudDTO> listarSolicitudesActivasPorFamiliar(Long familiarId); // Familiar

    // Métodos cambiar estado (Admin)
    Optional<SolicitudDTO> aprobarSolicitud(Long id, Long adminId);

    Optional<SolicitudDTO> rechazarSolicitud(Long id, Long adminId);

    // Borrado lógico (Admin o Familiar)
    void eliminarSolicitudLogico(Long id, Long usuarioId, String rolUsuario);

    SolicitudDTO actualizarSolicitud(Long id, SolicitudDTO solicitudDTO, Long familiarId)
            throws AccessDeniedException, IllegalStateException;

    void desactivarSolicitudesPorPaciente(Long pacienteId);
}