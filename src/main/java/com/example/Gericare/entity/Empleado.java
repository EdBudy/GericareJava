package com.example.gericare.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
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
    private String tipoContrato;
    private String contactoEmergencia;
    private LocalDate fechaNacimiento;
}