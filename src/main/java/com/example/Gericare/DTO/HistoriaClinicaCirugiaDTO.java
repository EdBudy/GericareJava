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
public class HistoriaClinicaCirugiaDTO {
    private Long idCirugia;
    private String descripcionCirugia;
    @NotNull(message = "La fecha de la cirugía es obligatoria.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @PastOrPresent(message = "La fecha de la cirugía no puede ser futura.")
    private LocalDate fechaCirugia;
    private String observaciones;
}