package com.example.Gericare.Service;

import com.example.Gericare.DTO.RolDTO;
import java.util.List;

public interface RolService {

    List<RolDTO> listarRols();

    RolDTO obtenerRolPorId(Long idRol);

    RolDTO crearRol(RolDTO RolDTO);

    RolDTO actualizarRol(Long idRol, RolDTO RolDTO);

    void eliminarRol(Long idRol);

}
