package com.estacionnexo.shared;

import com.estacionnexo.exception.ReglaIncumplida;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ImportesTest {
    @Test
    void conservaPrecisionDeLitros() {
        assertEquals(new BigDecimal("15.320"), Importes.litros(new BigDecimal("15.32")));
        assertThrows(ReglaIncumplida.class, () -> Importes.litros(new BigDecimal("1.0001")));
        assertThrows(ReglaIncumplida.class, () -> Importes.litros(BigDecimal.ZERO));
    }

    @Test
    void redondeaSubtotalesAlCentimo() {
        assertEquals(new BigDecimal("10.05"), Importes.subtotal(new BigDecimal("1.005"), BigDecimal.TEN));
    }
}
