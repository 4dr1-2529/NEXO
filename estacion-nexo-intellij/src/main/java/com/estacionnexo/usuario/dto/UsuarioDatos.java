package com.estacionnexo.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UsuarioDatos(@Positive long empleado, @Positive long rol,
                           @NotBlank @Pattern(regexp = "[A-Za-z0-9._-]{3,30}") String username,
                           @Size(max = 72) String password) {}
