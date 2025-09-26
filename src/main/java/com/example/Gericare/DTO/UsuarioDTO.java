package com.example.gericare.DTO;

import com.example.gericare.enums.EstadoUsuario;
import com.example.gericare.enums.TipoDocumento;
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

}
