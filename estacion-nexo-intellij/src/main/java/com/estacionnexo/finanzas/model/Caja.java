package com.estacionnexo.finanzas.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Caja(long id, LocalDate fecha, BigDecimal apertura, BigDecimal cierre, boolean abierta) {}
