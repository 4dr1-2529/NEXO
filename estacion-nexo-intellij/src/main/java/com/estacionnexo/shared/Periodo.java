package com.estacionnexo.shared;

import com.estacionnexo.config.TimeConfig;
import com.estacionnexo.exception.ReglaIncumplida;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record Periodo(LocalDate desde, LocalDate hasta, LocalDate hoy) {
    public static Periodo resolver(Clock reloj, LocalDate desde, LocalDate hasta) {
        var hoy = LocalDate.now(reloj.withZone(TimeConfig.ZONA_NEXO));
        var fin = hasta == null ? (desde == null || desde.isBefore(hoy) ? hoy.minusDays(1) : hoy) : hasta;
        var inicio = desde == null ? fin.minusDays(9) : desde;
        if (fin.isBefore(inicio) || ChronoUnit.DAYS.between(inicio, fin) > 365) {
            throw new ReglaIncumplida("Selecciona un periodo válido de hasta 366 días.");
        }
        return new Periodo(inicio, fin, hoy);
    }
}
