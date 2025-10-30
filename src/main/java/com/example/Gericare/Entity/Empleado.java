package com.example.Gericare.Entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import com.example.Gericare.Enums.TipoContrato;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DiscriminatorValue("EMPLEADO")
public abstract class Empleado extends Usuario {

    private LocalDate fechaContratacion;
    @Enumerated(EnumType.STRING)
    private TipoContrato tipoContrato;
    private String contactoEmergencia;
    private LocalDate fechaNacimiento;
}