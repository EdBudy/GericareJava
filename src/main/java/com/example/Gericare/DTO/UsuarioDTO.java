package com.example.Gericare.DTO;

import com.example.Gericare.entity.Rol;
import com.example.Gericare.enums.EstadoUsuario;
import com.example.Gericare.enums.TipoDocumento;
import lombok.*;

import java.time.LocalDate;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioDTO {

    private Long idUsuario;
    private TipoDocumento tipoDocumento;
    private String documentoIdentificacion;
    private String nombre;
    private String apellido;
    private LocalDate fechaNacimiento;
    private String direccion;
    private String correoElectronico;
    private String contrasena;
    private EstadoUsuario estado;
    private LocalDate fechaContratacion;
    private String tipoContrato;
    private String contactoEmergencia;
    private String parentesco;
    private Long idRol;
    private Rol rol;

}
