package com.estacionnexo.inventario.model;

import java.math.BigDecimal;

public record Existencia(long producto, BigDecimal litros, BigDecimal precio, boolean activo) {}
