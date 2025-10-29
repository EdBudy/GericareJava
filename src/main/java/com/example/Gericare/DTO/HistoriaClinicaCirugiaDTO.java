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
public class HistoriaClinicaCirugiaDTO {
    private Long idCirugia;
    private String descripcionCirugia;
    private LocalDate fechaCirugia;
    private String observaciones;
}