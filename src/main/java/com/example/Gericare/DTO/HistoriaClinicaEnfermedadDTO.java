package com.example.Gericare.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistoriaClinicaEnfermedadDTO {
    private Long idHcEnfermedad;
    private String descripcionEnfermedad;
    @NotNull(message = "La fecha de diagnóstico es obligatoria.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @PastOrPresent(message = "La fecha de diagnóstico no puede ser futura.")
    private LocalDate fechaDiagnostico;
    private String observaciones;
}