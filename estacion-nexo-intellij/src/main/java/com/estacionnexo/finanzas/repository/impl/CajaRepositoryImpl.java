package com.estacionnexo.finanzas.repository.impl;

import com.estacionnexo.exception.ReglaIncumplida;
import com.estacionnexo.finanzas.model.Caja;
import com.estacionnexo.finanzas.repository.CajaRepository;
import com.estacionnexo.shared.JdbcOperaciones;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CajaRepositoryImpl implements CajaRepository {
    private final JdbcTemplate jdbc;
    private final JdbcOperaciones operaciones;

    public CajaRepositoryImpl(JdbcTemplate jdbc, JdbcOperaciones operaciones) {
        this.jdbc = jdbc;
        this.operaciones = operaciones;
    }

    @Override
    public Optional<Caja> bloquear(LocalDate fecha) {
        return buscar(fecha, true);
    }

    @Override
    public Optional<Caja> buscar(LocalDate fecha) {
        return buscar(fecha, false);
    }

    private Optional<Caja> buscar(LocalDate fecha, boolean bloqueo) {
        String sql = """
            SELECT id_caja, fecha, saldo_apertura, saldo_cierre, estado
            FROM caja_diaria WHERE fecha = ?
            """;
        return jdbc.query(bloqueo ? sql + " FOR UPDATE" : sql,
            (rs, fila) -> new Caja(rs.getLong("id_caja"), rs.getObject("fecha", LocalDate.class),
            rs.getBigDecimal("saldo_apertura"), rs.getBigDecimal("saldo_cierre"),
            "ABIERTA".equals(rs.getString("estado"))), fecha).stream().findFirst();
    }

    @Override
    public long abrir(LocalDateTime fecha, long usuario, BigDecimal apertura) {
        return operaciones.insertar("""
            INSERT INTO caja_diaria (fecha, saldo_apertura, estado, id_usuario_apertura, fecha_apertura)
            VALUES (?, ?, 'ABIERTA', ?, ?)
            """, fecha.toLocalDate(), apertura, usuario, fecha);
    }

    @Override
    public BigDecimal saldo(Caja caja) {
        return jdbc.queryForObject("""
            SELECT ? + COALESCE(SUM(CASE tipo WHEN 'INGRESO' THEN monto ELSE -monto END), 0)
            FROM movimiento_caja WHERE id_caja = ?
            """, BigDecimal.class, caja.apertura(), caja.id());
    }

    @Override
    public void cerrar(Caja caja, long usuario, LocalDateTime fecha, BigDecimal saldo) {
        jdbc.update("""
            UPDATE caja_diaria SET estado='CERRADA', saldo_cierre=?, id_usuario_cierre=?, fecha_cierre=?
            WHERE id_caja=?
            """, saldo, usuario, fecha, caja.id());
    }

    @Override
    public void registrar(long caja, long usuario, Long compra, Long venta, BigDecimal monto,
                          LocalDateTime fecha, String codigo) {
        boolean ingreso = venta != null;
        String tipo = ingreso ? "INGRESO" : "EGRESO";
        String concepto = ingreso ? "VENTA" : "COMPRA";
        var ids = jdbc.queryForList("""
            SELECT id_concepto FROM concepto_movimiento WHERE codigo=? AND tipo=? AND estado='Activo'
            FOR UPDATE
            """, Long.class, concepto, tipo);
        if (ids.isEmpty()) {
            throw new ReglaIncumplida("El concepto económico de la operación está desactivado.");
        }
        jdbc.update("""
            INSERT INTO movimiento_caja
                (codigo_movimiento,id_caja,id_concepto,id_usuario,id_compra,id_venta,tipo,monto,descripcion,fecha_hora)
            VALUES (?,?,?,?,?,?,?,?,?,?)
            """, codigo, caja, ids.getFirst(), usuario, compra, venta, tipo, monto,
            ingreso ? "Venta de combustible" : "Compra de combustible", fecha);
    }

    @Override
    public void registrarManual(long caja, long usuario, long concepto, String tipo, BigDecimal monto,
                                String descripcion, LocalDateTime fecha, String codigo, String solicitud) {
        jdbc.update("""
            INSERT INTO movimiento_caja
                (codigo_movimiento,id_caja,id_concepto,id_usuario,id_compra,id_venta,tipo,monto,descripcion,fecha_hora,solicitud)
            VALUES (?,?,?,?,NULL,NULL,?,?,?,?,?)
            """, codigo, caja, concepto, usuario, tipo, monto, descripcion, fecha, solicitud);
    }
}
