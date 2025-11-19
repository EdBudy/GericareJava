package com.example.Gericare.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstadisticaActividadDTO {
    private String nombreCuidador;
    private String apellidoCuidador;
    private Long actividadesCompletadas;

    public String getNombreCompleto() {
        return nombreCuidador + " " + apellidoCuidador;
    }
}