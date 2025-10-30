package com.example.Gericare.Enums;

public enum TipoContrato {
    TERMINO_INDEFINIDO("Término Indefinido"),
    TERMINO_FIJO("Término Fijo"),
    OBRA_LABOR("Obra o Labor"),
    PRESTACION_DE_SERVICIOS("Prestación de Servicios"),
    APRENDIZAJE("Contrato de Aprendizaje");

    private final String descripcion;

    TipoContrato(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    // Permite Thymeleaf mostrar la descripción bonita
    @Override
    public String toString() {
        return descripcion;
    }
}
