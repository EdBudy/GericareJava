package com.example.Gericare.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmpleadoDTO extends UsuarioDTO {

    private LocalDate fechaContratacion;
    private String tipoContrato;
    private String contactoEmergencia;
    private LocalDate fechaNacimiento;
}