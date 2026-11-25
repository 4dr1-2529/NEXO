package com.estacionnexo.dashboard.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record Grafico(int ancho, List<Eje> ejeY, List<Eje> ejeX, List<Marca> marcas, String trazo) {
    private static final int IZQUIERDA = 78;
    private static final int DERECHA = 24;
    private static final int TECHO = 26;
    private static final int PISO = 193;
    private static final DateTimeFormatter FECHA_CORTA = DateTimeFormatter.ofPattern("dd/MM");

    public static Grafico linea(List<Dia> dias, String nombre, Function<Dia, BigDecimal> valor) {
        return construir(dias, nombre, valor, null);
    }

    public static Grafico caja(List<Dia> dias) {
        return construir(dias, "Ingresos", Dia::ingresos, Dia::egresos);
    }

    private static Grafico construir(List<Dia> dias, String nombre, Function<Dia, BigDecimal> valor,
                                     Function<Dia, BigDecimal> segunda) {
        int ancho = Math.max(640, IZQUIERDA + DERECHA + dias.size() * 35);
        double paso = (double) (ancho - IZQUIERDA - DERECHA) / Math.max(1, dias.size());
        List<BigDecimal> valores = dias.stream().flatMap(d -> segunda == null
            ? Stream.ofNullable(valor.apply(d)) : Stream.of(valor.apply(d), segunda.apply(d))).toList();
        var minimo = valores.stream().reduce(BigDecimal.ZERO, BigDecimal::min);
        var maximo = valores.stream().reduce(BigDecimal.ZERO, BigDecimal::max);
        if (minimo.compareTo(maximo) == 0) {
            maximo = maximo.add(BigDecimal.ONE);
        }
        var escala = new Escala(minimo, maximo);
        var ejeY = new ArrayList<Eje>();
        var ejeX = new ArrayList<Eje>();
        var marcas = new ArrayList<Marca>();
        for (int i = 0; i <= 4; i++) {
            var nivel = minimo.add(maximo.subtract(minimo).multiply(BigDecimal.valueOf(i))
                .divide(BigDecimal.valueOf(4), 2, RoundingMode.HALF_UP));
            ejeY.add(new Eje(escala.y(nivel), nivel.stripTrailingZeros().toPlainString()));
        }
        int salto = Math.max(1, 52 / (int) paso + 1);
        int base = escala.y(BigDecimal.ZERO);
        for (int i = 0; i < dias.size(); i++) {
            var dia = dias.get(i);
            int x = (int) Math.round(IZQUIERDA + (i + .5) * paso);
            if (i % salto == 0 || i == dias.size() - 1) {
                ejeX.add(new Eje(x, FECHA_CORTA.format(dia.fecha())));
            }
            var primero = valor.apply(dia);
            if (primero != null) {
                int columna = (int) Math.min(18, paso * .32);
                int posicion = segunda == null ? x : x - columna - 2;
                marcas.add(marca(posicion, escala.y(primero), columna, base, primero,
                    nombre, dia, false));
                if (segunda != null) {
                    var egreso = segunda.apply(dia);
                    marcas.add(marca(x + 2, escala.y(egreso), columna, base, egreso,
                        "Egresos", dia, true));
                }
            }
        }
        String trazo = segunda == null ? marcas.stream().map(p -> p.x() + "," + p.y())
            .collect(Collectors.joining(" ")) : "";
        return new Grafico(ancho, ejeY, ejeX, marcas, trazo);
    }

    private static Marca marca(int x, int y, int ancho, int base, BigDecimal valor, String nombre,
                               Dia dia, boolean secundaria) {
        String monto = "S/ " + valor.setScale(2, RoundingMode.HALF_UP).toPlainString();
        return new Marca(x, y, ancho, Math.max(0, base - y), monto, nombre + " · " + dia.fecha() + " · " + monto, secundaria);
    }

    public record Eje(int posicion, String etiqueta) {}
    public record Marca(int x, int y, int ancho, int alto, String valor, String etiqueta, boolean secundaria) {}

    private record Escala(BigDecimal minimo, BigDecimal maximo) {
        int y(BigDecimal valor) {
            double avance = valor.subtract(minimo).doubleValue() / maximo.subtract(minimo).doubleValue();
            return (int) Math.round(PISO - avance * (PISO - TECHO));
        }
    }
}
