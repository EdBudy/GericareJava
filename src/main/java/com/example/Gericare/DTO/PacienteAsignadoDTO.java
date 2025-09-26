package com.example.gericare.DTO;

import com.example.gericare.enums.EstadoAsignacion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PacienteAsignadoDTO {
    private Long idAsignacion;
    private PacienteDTO paciente;
    private UsuarioDTO cuidador;
    private UsuarioDTO familiar;
    private EstadoAsignacion estado;
    private LocalDateTime fechaCreacion;
    private String nombreAdminCreador; // Solo se muestra el nombre.
}