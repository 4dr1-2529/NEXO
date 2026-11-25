package com.estacionnexo.finanzas.service;

import com.estacionnexo.finanzas.model.Caja;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface CajaService {
    long abrir(long usuario, BigDecimal apertura);
    BigDecimal cerrar(long usuario);
    Optional<Caja> actual();
    void movimientoManual(long usuario, long conceptoId, BigDecimal monto, String descripcion, UUID solicitud);
}
