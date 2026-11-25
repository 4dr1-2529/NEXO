package com.estacionnexo.dashboard;

import com.estacionnexo.dashboard.model.CombustiblePanel;
import com.estacionnexo.dashboard.model.Composicion;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ComposicionTest {
    @Test
    void conservaImportesYProporciones() {
        var resultado = Composicion.de(List.of(combustible("Diésel", "75"), combustible("Regular", "25")));
        assertEquals(new BigDecimal("75.00"), resultado.segmentos().getFirst().participacion());
        assertEquals(new BigDecimal("25.00"), resultado.segmentos().getLast().participacion());
        assertTrue(resultado.gradiente().contains("75.00% 100.00%"));
    }

    @Test
    void agrupaLosProductosRestantesYAdmiteElPeriodoVacio() {
        var datos = java.util.stream.IntStream.range(0, 8).mapToObj(i -> combustible("P" + i, "10")).toList();
        var resultado = Composicion.de(datos);
        assertEquals(6, resultado.segmentos().size());
        assertEquals(new BigDecimal("30"), resultado.segmentos().getLast().litros());
        assertTrue(Composicion.de(List.of()).segmentos().isEmpty());
    }

    @Test
    void noMezclaGalonesConLitrosEnElGrafico() {
        var resultado = Composicion.de(List.of(
            new CombustiblePanel("GAS-REG", "Regular", new BigDecimal("100"), null, "L"),
            new CombustiblePanel("GLP-AUTO", "GLP", new BigDecimal("50"), null, "gal")));
        assertEquals(1, resultado.segmentos().size());
        assertEquals("L", resultado.unidadGrafico());
        assertEquals(new BigDecimal("100"), resultado.totalGrafico());
        assertEquals("Regular", resultado.segmentos().getFirst().nombre());
    }

    private CombustiblePanel combustible(String nombre, String litros) {
        return new CombustiblePanel(nombre, nombre, new BigDecimal(litros), null, "L");
    }
}
