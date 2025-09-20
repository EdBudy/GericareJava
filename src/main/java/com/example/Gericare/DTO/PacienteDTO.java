package com.example.Gericare.DTO;

import com.example.Gericare.enums.TipoSangre;
import com.example.Gericare.enums.Genero;
import com.example.Gericare.enums.EstadoPaciente;
import com.example.Gericare.enums.TipoSangreConverter;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;


@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PacienteDTO {

    public class Paciente {

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

}
