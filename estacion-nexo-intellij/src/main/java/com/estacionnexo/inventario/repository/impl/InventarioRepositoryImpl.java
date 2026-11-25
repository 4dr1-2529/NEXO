package com.estacionnexo.inventario.repository.impl;

import com.estacionnexo.inventario.model.Existencia;
import com.estacionnexo.inventario.repository.InventarioRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class InventarioRepositoryImpl implements InventarioRepository {
    private final JdbcTemplate jdbc;

    public InventarioRepositoryImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<Existencia> bloquearProducto(long id) {
        return jdbc.query("""
            SELECT p.id_producto, p.stock_actual, p.precio_venta,
                   (p.estado = 'Activo' AND c.estado = 'Activo') AS disponible
            FROM producto p JOIN categoria c ON c.id_categoria = p.id_categoria
            WHERE p.id_producto = ? FOR UPDATE
            """, (rs, fila) -> new Existencia(rs.getLong("id_producto"),
                rs.getBigDecimal("stock_actual"), rs.getBigDecimal("precio_venta"),
                rs.getBoolean("disponible")), id).stream().findFirst();
    }

    @Override
    public boolean cambiarStock(long id, BigDecimal variacion) {
        return jdbc.update("""
            UPDATE producto SET stock_actual = stock_actual + ?
            WHERE id_producto = ? AND stock_actual + ? BETWEEN 0 AND 999999999.999
            """, variacion, id, variacion) == 1;
    }

    @Override
    public void registrar(long producto, long usuario, Long compra, Long venta, String tipo,
                          BigDecimal litros, LocalDateTime fecha, String motivo, String solicitud) {
        jdbc.update("""
            INSERT INTO movimiento_inventario
                (id_producto, id_usuario, id_detalle_compra, id_detalle_venta,
                 tipo, cantidad_litros, fecha_hora, motivo, solicitud)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, producto, usuario, compra, venta, tipo, litros, fecha, motivo, solicitud);
    }
}
