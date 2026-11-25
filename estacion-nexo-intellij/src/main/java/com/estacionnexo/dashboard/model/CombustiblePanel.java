package com.estacionnexo.dashboard.model;

import java.math.BigDecimal;

public record CombustiblePanel(String codigo, String nombre, BigDecimal cantidad, BigDecimal minimo, String unidad) {
    public CombustiblePanel(String codigo, String nombre, BigDecimal cantidad, BigDecimal minimo) {
        this(codigo, nombre, cantidad, minimo, "L");
    }

    public boolean bajo() {
        return minimo != null && cantidad.compareTo(minimo) < 0;
    }

    /** Compatibilidad con código que aún nombra el volumen como litros. */
    @Deprecated
    public BigDecimal litros() {
        return cantidad;
    }
}
