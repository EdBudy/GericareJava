package com.example.gericare.DTO;

import com.example.gericare.enums.TipoSangre;
import com.example.gericare.enums.Genero;
import com.example.gericare.enums.EstadoPaciente;
import lombok.*;
import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PacienteDTO {

    private Long idPaciente;
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
