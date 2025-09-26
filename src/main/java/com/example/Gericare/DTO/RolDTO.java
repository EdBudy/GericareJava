package com.example.gericare.DTO;

import com.example.gericare.enums.RolNombre;
import lombok.*;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RolDTO {

    private Long idRol;
    private RolNombre rolNombre;
    private String descripcion;
}
