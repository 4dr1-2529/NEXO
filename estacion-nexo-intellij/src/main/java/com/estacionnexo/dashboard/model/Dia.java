package com.estacionnexo.dashboard.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Dia(LocalDate fecha, BigDecimal ventas, BigDecimal litros, long cantidadVentas,
                  BigDecimal ingresos, BigDecimal egresos, BigDecimal saldoCierre,
                  BigDecimal saldoActual, boolean cajaAbierta, boolean tieneCaja) {}
