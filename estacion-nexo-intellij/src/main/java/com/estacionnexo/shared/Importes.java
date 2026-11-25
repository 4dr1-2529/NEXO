package com.estacionnexo.shared;

import com.estacionnexo.exception.ReglaIncumplida;
import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Importes {
    private Importes() {}

    public static BigDecimal importe(BigDecimal valor, boolean admiteCero) {
        return verificar(valor, 2, admiteCero);
    }

    public static BigDecimal litros(BigDecimal valor) {
        return verificar(valor, 3, false);
    }

    public static BigDecimal subtotal(BigDecimal litros, BigDecimal precio) {
        return importe(litros.multiply(precio).setScale(2, RoundingMode.HALF_UP), false);
    }

    private static BigDecimal verificar(BigDecimal valor, int escala, boolean admiteCero) {
        if (valor == null || valor.signum() < 0 || (!admiteCero && valor.signum() == 0)
                || valor.stripTrailingZeros().scale() > escala
                || valor.compareTo(BigDecimal.TEN.pow(12 - escala)) >= 0) {
            throw new ReglaIncumplida("Cantidad o importe fuera del rango permitido.");
        }
        return valor.setScale(escala);
    }
}
