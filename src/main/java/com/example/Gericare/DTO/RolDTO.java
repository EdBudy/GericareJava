package com.example.Gericare.DTO;

import com.example.Gericare.enums.RolNombre;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RolDTO {

    private Long idRol;
    private RolNombre rolNombre;
    private String descripcion;
}
