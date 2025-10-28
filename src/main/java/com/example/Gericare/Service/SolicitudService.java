package com.example.Gericare.Service;

import com.example.Gericare.DTO.SolicitudDTO;
import com.example.Gericare.Enums.EstadoSolicitud;

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

    // Desactivación cascada
    void desactivarSolicitudesPorPaciente(Long pacienteId);

}