package com.example.Gericare.Enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum EstadoCivil {

    SOLTERO("Soltero"),
    CASADO("Casado"),
    UNION_LIBRE("Unión Libre"),
    DIVORCIADO("Divorciado"),
    VIUDO("Viudo");

    private final String texto;

    EstadoCivil(String texto) {
        this.texto = texto;
    }

    // @JsonValue
    // Hace que cuando el Back envíe este dato al Frontend,
    // envíe "Unión Libre" en vez de "UNION_LIBRE".
    @JsonValue
    public String getTexto() {
        return texto;
    }
}