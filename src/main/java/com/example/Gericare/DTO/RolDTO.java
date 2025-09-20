package com.example.Gericare.DTO;

import com.example.Gericare.enums.RolNombre;
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
