package com.example.Gericare.DTO;

import com.example.Gericare.enums.TipoSangre;
import com.example.Gericare.enums.Genero;
import com.example.Gericare.enums.EstadoPaciente;
import lombok.*;
import java.time.LocalDate;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PacienteDTO {

    private Long idPaciente;
    @Size(min = 5, max = 15, message = "El documento debe tener entre 5 y 15 caracteres")
    @Pattern(regexp = "[0-9]+", message = "El documento solo debe contener números")
    private String documentoIdentificacion;
    private String nombre;
    private String apellido;
    private LocalDate fechaNacimiento;
    private Genero genero;
    @Pattern(regexp = "[0-9]+", message = "El contacto de emergencia solo debe contener números")
    private String contactoEmergencia;
    private String estadoCivil;
    private TipoSangre tipoSangre;
    private String seguroMedico;
    private String numeroSeguro;
    private EstadoPaciente estado;
    private String nombreFamiliarAsociado;

}
