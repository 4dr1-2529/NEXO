package com.estacionnexo.dashboard.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public record Composicion(String gradiente, List<Segmento> segmentos, String unidadGrafico, BigDecimal totalGrafico) {
    private static final List<String> PALETA = List.of("#b7ed81", "#83c8ef", "#efbd82", "#c9ade8", "#efa8ab", "#a7b8b0");

    public static Composicion de(List<CombustiblePanel> combustibles) {
        // Solo se grafica una unidad a la vez (preferencia L) para no mezclar L/gal/m3.
        var base = combustibles.stream().filter(c -> "L".equals(c.unidad())).toList();
        if (base.isEmpty()) {
            base = combustibles;
        }
        if (base.isEmpty()) {
            return new Composicion("", List.of(), "L", BigDecimal.ZERO);
        }
        String unidad = base.getFirst().unidad() == null ? "L" : base.getFirst().unidad();
        var visibles = new ArrayList<>(base.subList(0, Math.min(5, base.size())));
        if (base.size() > 5) {
            var resto = base.subList(5, base.size()).stream().map(CombustiblePanel::cantidad)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            visibles.add(new CombustiblePanel("", "Otros combustibles", resto, null, unidad));
        }
        var total = visibles.stream().map(CombustiblePanel::cantidad).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (total.signum() == 0) {
            return new Composicion("", List.of(), unidad, BigDecimal.ZERO);
        }
        var gradual = new StringJoiner(", ", "conic-gradient(", ")");
        var segmentos = new ArrayList<Segmento>();
        var inicio = BigDecimal.ZERO;
        var acumulado = BigDecimal.ZERO;
        for (int i = 0; i < visibles.size(); i++) {
            var dato = visibles.get(i);
            acumulado = acumulado.add(dato.cantidad());
            var fin = porcentaje(acumulado, total);
            var color = PALETA.get(i);
            gradual.add(color + " " + inicio.toPlainString() + "% " + fin.toPlainString() + "%");
            segmentos.add(new Segmento(dato.nombre(), dato.cantidad(), porcentaje(dato.cantidad(), total), color, unidad));
            inicio = fin;
        }
        String fondo = segmentos.size() == 1 ? segmentos.getFirst().color() : gradual.toString();
        return new Composicion(fondo, List.copyOf(segmentos), unidad, total);
    }

    private static BigDecimal porcentaje(BigDecimal cantidad, BigDecimal total) {
        return cantidad.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP);
    }

    public record Segmento(String nombre, BigDecimal litros, BigDecimal participacion, String color, String unidad) {
        public Segmento(String nombre, BigDecimal litros, BigDecimal participacion, String color) {
            this(nombre, litros, participacion, color, "L");
        }
    }
}
