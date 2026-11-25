package com.estacionnexo.compra.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface CompraRepository {
    boolean proveedorDisponible(long proveedor);
    long registrar(String codigo, Long proveedor, long usuario, LocalDateTime fecha,
                   BigDecimal total, String observacion, String solicitud);
    long registrarDetalle(long compra, long producto, BigDecimal litros, BigDecimal precio, BigDecimal subtotal);
}
