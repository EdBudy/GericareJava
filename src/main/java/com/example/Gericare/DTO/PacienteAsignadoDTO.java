package com.example.Gericare.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PacienteAsignadoDTO {
    private Long idAsignacion;

    private PacienteDTO paciente;
    private UsuarioDTO cuidador;
    private UsuarioDTO familiar;
}