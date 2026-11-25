package com.estacionnexo.shared.consulta.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Registro(long id, String codigo, LocalDateTime fecha, String responsable, String referencia,
                       String estado, BigDecimal valor) {}
