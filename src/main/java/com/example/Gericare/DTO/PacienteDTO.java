package com.example.Gericare.DTO;

import com.example.Gericare.enums.TipoSangre;
import com.example.Gericare.enums.Genero;
import com.example.Gericare.enums.EstadoPaciente;
import lombok.*;
import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PacienteDTO {

    private Long id;
    private String documentoIdentificacion;
    private String nombre;
    private String apellido;
    private LocalDate fechaNacimiento;
    private Genero genero;
    private String contactoEmergencia;
    private String estadoCivil;
    private TipoSangre tipoSangre;
    private String seguroMedico;
    private String numeroSeguro;
    private EstadoPaciente estado;
    private String nombreFamiliarAsociado;

}
