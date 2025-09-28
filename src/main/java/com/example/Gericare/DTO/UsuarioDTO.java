package com.example.Gericare.DTO;

import com.example.Gericare.entity.Rol;
import com.example.Gericare.enums.EstadoUsuario;
import com.example.Gericare.enums.TipoDocumento;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {

    // Campos Comunes
    private Long idUsuario;
    private TipoDocumento tipoDocumento;
    private String documentoIdentificacion;
    private String nombre;
    private String apellido;
    private String direccion;
    private String correoElectronico;
    private String contrasena;
    private EstadoUsuario estado;
    private Rol rol;

    // Campos de Empleado
    private LocalDate fechaContratacion;
    private String tipoContrato;
    private String contactoEmergencia;
    private LocalDate fechaNacimiento;

    // Campos de Familiar
    private String parentesco;
}