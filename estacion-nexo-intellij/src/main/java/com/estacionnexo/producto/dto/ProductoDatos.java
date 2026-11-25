package com.estacionnexo.producto.dto;

import com.estacionnexo.producto.CatalogoCombustibles;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Locale;

public record ProductoDatos(
        @NotBlank @Pattern(regexp = "[A-Z0-9]+(?:-[A-Z0-9]+)*",
            message = "Usa letras, números y guiones entre segmentos.") @Size(max = 10) String codigo,
        @NotNull(message = "Selecciona una categoría.") @Positive Long categoria,
        @NotBlank @Size(max = 60) String nombre,
        @NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal precio,
        @PositiveOrZero @Digits(integer = 9, fraction = 3) BigDecimal stockMinimo,
        @NotBlank @Pattern(regexp = "L|gal|m3", message = "Usa L, gal o m3.") @Size(max = 10) String unidadMedida) {

    public ProductoDatos(String codigo, Long categoria, String nombre, BigDecimal precio) {
        this(codigo, categoria, nombre, precio, null, unidadPorDefecto(codigo));
    }

    public ProductoDatos(String codigo, Long categoria, String nombre, BigDecimal precio, String unidadMedida) {
        this(codigo, categoria, nombre, precio, null, unidadMedida);
    }

    public ProductoDatos {
        if (codigo != null) {
            codigo = codigo.strip().toUpperCase(Locale.ROOT);
        }
        if (unidadMedida == null || unidadMedida.isBlank()) {
            unidadMedida = unidadPorDefecto(codigo);
        } else {
            unidadMedida = unidadMedida.strip();
        }
        if (!CatalogoCombustibles.unidadValida(unidadMedida)) {
            throw new IllegalArgumentException("Unidad de medida no admitida: " + unidadMedida);
        }
    }

    private static String unidadPorDefecto(String codigo) {
        return CatalogoCombustibles.porCodigo(codigo).map(CatalogoCombustibles.Item::unidad).orElse("L");
    }
}
