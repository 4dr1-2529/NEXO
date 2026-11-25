package com.estacionnexo.inventario.service;

import java.math.BigDecimal;
import java.util.UUID;

public interface InventarioService {
    enum Tipo { ENTRADA, SALIDA }
    void ajustar(long usuario, long producto, Tipo tipo, BigDecimal litros, String motivo, UUID solicitud);
}
