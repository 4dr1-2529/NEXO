package com.estacionnexo.inventario.repository;

import com.estacionnexo.inventario.model.Existencia;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public interface InventarioRepository {
    Optional<Existencia> bloquearProducto(long id);
    boolean cambiarStock(long id, BigDecimal variacion);
    void registrar(long producto, long usuario, Long compra, Long venta, String tipo,
                   BigDecimal litros, LocalDateTime fecha, String motivo, String solicitud);
}
