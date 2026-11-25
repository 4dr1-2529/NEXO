package com.estacionnexo.compra.repository.impl;

import com.estacionnexo.compra.repository.CompraRepository;
import com.estacionnexo.shared.JdbcOperaciones;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CompraRepositoryImpl implements CompraRepository {
    private final JdbcTemplate jdbc;
    private final JdbcOperaciones operaciones;

    public CompraRepositoryImpl(JdbcTemplate jdbc, JdbcOperaciones operaciones) {
        this.jdbc = jdbc;
        this.operaciones = operaciones;
    }

    @Override
    public boolean proveedorDisponible(long proveedor) {
        return !jdbc.queryForList("""
            SELECT id_proveedor FROM proveedor WHERE id_proveedor=? AND estado='Activo' FOR UPDATE
            """, Long.class, proveedor).isEmpty();
    }

    @Override
    public long registrar(String codigo, Long proveedor, long usuario, LocalDateTime fecha,
                          BigDecimal total, String observacion, String solicitud) {
        return operaciones.insertar("""
            INSERT INTO compra (codigo_compra,id_proveedor,id_usuario,fecha_hora,total,estado,observacion,solicitud)
            VALUES (?,?,?,?,?,'Confirmada',?,?)
            """, codigo, proveedor, usuario, fecha, total, observacion, solicitud);
    }

    @Override
    public long registrarDetalle(long compra, long producto, BigDecimal litros, BigDecimal precio, BigDecimal subtotal) {
        return operaciones.insertar("""
            INSERT INTO detalle_compra (id_compra,id_producto,cantidad_litros,precio_compra,subtotal)
            VALUES (?,?,?,?,?)
            """, compra, producto, litros, precio, subtotal);
    }
}
