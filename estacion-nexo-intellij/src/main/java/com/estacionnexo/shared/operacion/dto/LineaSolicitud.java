package com.estacionnexo.shared.operacion.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record LineaSolicitud(@Positive long producto, @NotNull @Positive BigDecimal litros,
                              @Positive BigDecimal precio) {}
