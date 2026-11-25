package com.estacionnexo.producto;

import java.util.List;
import java.util.Optional;

/** Definición canónica del catálogo activo (sin precios ni stock). */
public final class CatalogoCombustibles {
    public static final String CAT_GASOLINAS = "Gasolinas";
    public static final String CAT_DIESEL = "Diésel";
    public static final String CAT_GASES = "Gases vehiculares";

    public static final String DESC_GASOLINAS =
        "Gasolina Regular (90 RON, rojo) y Premium (95 RON, amarillo).";
    public static final String DESC_DIESEL =
        "Diésel B5 S-50 para vehículos con motor diésel, incluidos carga y transporte.";
    public static final String DESC_GASES =
        "GLP para uso automotor (gal) y GNV comprimido (m3). No mezclar cantidades entre unidades distintas.";

    public record Item(String codigo, String categoria, String nombre, String unidad) {}

    public static final List<Item> ACTIVOS = List.of(
        new Item("GAS-REG", CAT_GASOLINAS, "Gasolina Regular", "L"),
        new Item("GAS-PRE", CAT_GASOLINAS, "Gasolina Premium", "L"),
        new Item("DIE-B5S50", CAT_DIESEL, "Diésel B5 S-50", "L"),
        new Item("GLP-AUTO", CAT_GASES, "GLP para uso automotor", "gal"),
        new Item("GNV-C", CAT_GASES, "Gas Natural Vehicular Comprimido", "m3"));

    private CatalogoCombustibles() {}

    public static Optional<Item> porCodigo(String codigo) {
        if (codigo == null) {
            return Optional.empty();
        }
        return ACTIVOS.stream().filter(i -> i.codigo().equalsIgnoreCase(codigo.strip())).findFirst();
    }

    public static boolean unidadValida(String unidad) {
        return "L".equals(unidad) || "gal".equals(unidad) || "m3".equals(unidad);
    }
}
