package com.estacionnexo.dashboard.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public record AnalisisVentas(List<Ticket> tickets, BigDecimal maximoTicket, List<Punto> puntos,
                             BigDecimal maximoLitros, BigDecimal maximoVentas, List<Semana> semana) {
    public static AnalisisVentas de(List<Dia> dias) {
        var activos = dias.stream().filter(d -> d.cantidadVentas() > 0).toList();
        var maxLitros = activos.stream().map(Dia::litros).reduce(BigDecimal.ZERO, BigDecimal::max);
        var maxVentas = activos.stream().map(Dia::ventas).reduce(BigDecimal.ZERO, BigDecimal::max);
        var importes = dias.stream().map(d -> d.cantidadVentas() == 0 ? null :
            d.ventas().divide(BigDecimal.valueOf(d.cantidadVentas()), 2, RoundingMode.HALF_UP)).toList();
        var maxTicket = importes.stream().filter(java.util.Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::max);
        var tickets = java.util.stream.IntStream.range(0, dias.size()).mapToObj(i ->
            new Ticket(dias.get(i).fecha().toString(), importes.get(i), escala(importes.get(i), maxTicket, 100))).toList();
        var puntos = activos.stream().map(d -> new Punto(54 + escala(d.litros(), maxLitros, 310),
            178 - escala(d.ventas(), maxVentas, 144),
            d.fecha() + " · " + d.litros().toPlainString() + " L · S/ " + d.ventas().toPlainString())).toList();

        var promedios = Arrays.stream(DayOfWeek.values()).map(dia -> {
            var jornadas = dias.stream().filter(d -> d.fecha().getDayOfWeek() == dia).toList();
            var litros = jornadas.stream().map(Dia::litros).reduce(BigDecimal.ZERO, BigDecimal::add);
            return jornadas.isEmpty() ? null : litros.divide(BigDecimal.valueOf(jornadas.size()), 3, RoundingMode.HALF_UP);
        }).toList();
        var mayor = promedios.stream().filter(java.util.Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::max);
        var semana = Arrays.stream(DayOfWeek.values()).map(dia -> {
            var valor = promedios.get(dia.getValue() - 1);
            return new Semana(dia.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es-PE")),
                valor, escala(valor, mayor, 100));
        }).toList();
        return new AnalisisVentas(tickets, maxTicket, puntos, maxLitros, maxVentas, semana);
    }

    private static int escala(BigDecimal valor, BigDecimal maximo, int ancho) {
        return valor == null || maximo.signum() == 0 ? 0
            : valor.multiply(BigDecimal.valueOf(ancho)).divide(maximo, 0, RoundingMode.HALF_UP).intValueExact();
    }

    public record Ticket(String fecha, BigDecimal importe, int altura) {}
    public record Punto(int x, int y, String detalle) {}
    public record Semana(String dia, BigDecimal litros, int ancho) {}
}