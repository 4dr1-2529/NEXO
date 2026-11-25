package com.estacionnexo.categoria.dto;

import jakarta.validation.constraints.*;

public record CategoriaDatos(@NotBlank @Size(max = 50) String nombre, @Size(max = 200) String descripcion) {}
