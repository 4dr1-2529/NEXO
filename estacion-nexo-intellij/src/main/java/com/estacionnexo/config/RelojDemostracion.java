package com.estacionnexo.config;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

final class RelojDemostracion extends Clock {
    private final Clock normal;
    private final ThreadLocal<Instant> historico;

    RelojDemostracion() {
        this(Clock.system(ZoneId.of("America/Lima")), new ThreadLocal<>());
    }

    private RelojDemostracion(Clock normal, ThreadLocal<Instant> historico) {
        this.normal = normal;
        this.historico = historico;
    }

    @Override
    public ZoneId getZone() {
        return normal.getZone();
    }

    @Override
    public Clock withZone(ZoneId zona) {
        return new RelojDemostracion(normal.withZone(zona), historico);
    }

    @Override
    public Instant instant() {
        var instante = historico.get();
        return instante == null ? normal.instant() : instante;
    }

    void en(LocalDateTime fecha, Runnable tarea) {
        var anterior = historico.get();
        historico.set(fecha.atZone(getZone()).toInstant());
        try {
            tarea.run();
        } finally {
            if (anterior == null) {
                historico.remove();
            } else {
                historico.set(anterior);
            }
        }
    }
}
