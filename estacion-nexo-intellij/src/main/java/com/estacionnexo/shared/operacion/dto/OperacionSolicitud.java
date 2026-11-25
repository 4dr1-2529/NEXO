package com.estacionnexo.shared.operacion.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record OperacionSolicitud(@NotNull UUID solicitud, Long proveedor, @Size(max = 200) String observacion,
        @NotNull @Size(min = 1, max = 50) List<@NotNull @Valid LineaSolicitud> lineas) {}
