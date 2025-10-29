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
public class HistoriaClinicaEnfermedadDTO {
    private Long idHcEnfermedad;
    private Long idEnfermedad; // Para la selección
    private String nombreEnfermedad; // Para mostrar
    private LocalDate fechaDiagnostico;
    private String observaciones;
}