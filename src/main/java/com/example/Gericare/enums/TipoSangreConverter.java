package com.example.Gericare.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.stream.Stream;

@Converter(autoApply = true) // autoApply lo hace automático para cualquier campo TipoSangre
public class TipoSangreConverter implements AttributeConverter<TipoSangre, String> {

    // Método para convertir de Enum a String (para guardar en la BD)
    @Override
    public String convertToDatabaseColumn(TipoSangre tipoSangre) {
        if (tipoSangre == null) {
            return null;
        }
        return tipoSangre.getValor(); // Guarda "A+", "O-", etc.
    }

    // Método para convertir de String (leído de la BD) a Enum
    @Override
    public TipoSangre convertToEntityAttribute(String valor) {
        if (valor == null) {
            return null;
        }
        return Stream.of(TipoSangre.values())
                .filter(c -> c.getValor().equals(valor))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
