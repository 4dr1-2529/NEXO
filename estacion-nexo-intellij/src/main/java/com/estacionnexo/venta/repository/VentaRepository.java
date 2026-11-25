package com.estacionnexo.venta.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface VentaRepository {
    long registrar(String codigo, long usuario, LocalDateTime fecha, BigDecimal total, String solicitud);
    long registrarDetalle(long venta, long producto, BigDecimal litros, BigDecimal precio, BigDecimal subtotal);
}
