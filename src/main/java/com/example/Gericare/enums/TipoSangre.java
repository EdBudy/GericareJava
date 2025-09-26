package com.example.gericare.enums;

public enum TipoSangre {
    A_POSITIVO("A+"),
    A_NEGATIVO("A-"),
    B_POSITIVO("B+"),
    B_NEGATIVO("B-"),
    AB_POSITIVO("AB+"),
    AB_NEGATIVO("AB-"),
    O_POSITIVO("O+"),
    O_NEGATIVO("O-");

    private final String valor;

    // Constructor para asociar el String a cada enum
    TipoSangre(String valor) {
        this.valor = valor;
    }

    // Método para obtener el String ("A+", "B-", etc.)
    public String getValor() {
        return valor;
    }
}
