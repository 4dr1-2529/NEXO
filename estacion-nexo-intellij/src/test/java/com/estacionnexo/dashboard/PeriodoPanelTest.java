package com.estacionnexo.dashboard;

import com.estacionnexo.shared.Periodo;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PeriodoPanelTest {
    private final Clock reloj = Clock.fixed(Instant.parse("2026-11-24T03:00:00Z"), ZoneOffset.UTC);

    @Test
    void usaDiezDiasCompletosEnLima() {
        var periodo = Periodo.resolver(reloj, null, null);
        assertEquals(LocalDate.of(2026, 11, 23), periodo.hoy());
        assertEquals(LocalDate.of(2026, 11, 13), periodo.desde());
        assertEquals(LocalDate.of(2026, 11, 22), periodo.hasta());
    }

    @Test
    void aceptaConsultarHoy() {
        var hoy = LocalDate.of(2026, 11, 23);
        assertEquals(new Periodo(hoy, hoy, hoy), Periodo.resolver(reloj, hoy, hoy));
    }
}
