package com.estacionnexo.shared.consulta.model;

import java.math.BigDecimal;

public record Detalle(String producto, BigDecimal litros, BigDecimal precio, BigDecimal subtotal) {}
