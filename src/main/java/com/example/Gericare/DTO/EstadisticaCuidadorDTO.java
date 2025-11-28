package com.example.Gericare.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstadisticaCuidadorDTO {
    private String nombreCuidador;
    private String apellidoCuidador;
    private Long cantidadPacientes;
}