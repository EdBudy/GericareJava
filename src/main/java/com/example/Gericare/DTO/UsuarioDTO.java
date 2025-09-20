package com.example.Gericare.DTO;

import com.example.Gericare.entity.Paciente;
import com.example.Gericare.entity.Rol;
import com.example.Gericare.enums.EstadoUsuario;
import com.example.Gericare.enums.TipoDocumento;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioDTO {

    private Integer id;
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
