package com.estacionnexo.venta.repository.impl;

import com.estacionnexo.shared.JdbcOperaciones;
import com.estacionnexo.venta.repository.VentaRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.stereotype.Repository;

@Repository
public class VentaRepositoryImpl implements VentaRepository {
    private final JdbcOperaciones operaciones;

    public VentaRepositoryImpl(JdbcOperaciones operaciones) {
        this.operaciones = operaciones;
    }

    @Override
    public long registrar(String codigo, long usuario, LocalDateTime fecha, BigDecimal total, String solicitud) {
        return operaciones.insertar("""
            INSERT INTO venta (codigo_venta,id_usuario,fecha_hora,total,estado,solicitud)
            VALUES (?,?,?,?,'Confirmada',?)
            """, codigo, usuario, fecha, total, solicitud);
    }

    @Override
    public long registrarDetalle(long venta, long producto, BigDecimal litros, BigDecimal precio, BigDecimal subtotal) {
        return operaciones.insertar("""
            INSERT INTO detalle_venta (id_venta,id_producto,cantidad_litros,precio_unitario,subtotal)
            VALUES (?,?,?,?,?)
            """, venta, producto, litros, precio, subtotal);
    }
}
