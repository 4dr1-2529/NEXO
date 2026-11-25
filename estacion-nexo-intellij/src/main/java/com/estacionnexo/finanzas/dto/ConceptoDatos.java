package com.estacionnexo.finanzas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ConceptoDatos(@NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{1,10}") String codigo,
                            @NotBlank @Size(max = 60) String nombre, @NotNull Tipo tipo) {
    public enum Tipo { INGRESO, EGRESO }
}
