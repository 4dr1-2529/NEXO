package com.estacionnexo.dashboard.repository.impl;

import com.estacionnexo.dashboard.model.CombustiblePanel;
import com.estacionnexo.dashboard.model.Dia;
import com.estacionnexo.dashboard.model.ProveedorCompra;
import com.estacionnexo.dashboard.repository.DashboardRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class DashboardRepositoryImpl implements DashboardRepository {
    private static final RowMapper<CombustiblePanel> COMBUSTIBLE = (rs, i) -> new CombustiblePanel(
        rs.getString("codigo_producto"), rs.getString("nombre"), rs.getBigDecimal("cantidad"),
        rs.getBigDecimal("minimo"), rs.getString("unidad"));
    private final JdbcTemplate jdbc;

    public DashboardRepositoryImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<CombustiblePanel> existencias() {
        return jdbc.query("""
            SELECT p.codigo_producto, p.nombre, p.stock_actual cantidad, p.stock_minimo minimo, p.unidad_medida unidad
            FROM producto p JOIN categoria c ON c.id_categoria=p.id_categoria
            WHERE p.estado='Activo' AND c.estado='Activo'
              AND c.nombre IN ('Gasolinas','Diésel','Gases vehiculares')
              AND p.codigo_producto IN ('GAS-REG','GAS-PRE','DIE-B5S50','GLP-AUTO','GNV-C')
            ORDER BY p.stock_actual DESC, p.nombre, p.id_producto
            """, COMBUSTIBLE);
    }

    @Override
    public List<CombustiblePanel> litrosVendidos(LocalDate desde, LocalDate hasta, Long vendedor) {
        // Histórico: incluye productos/categorías archivados; no filtra por SKU canónico.
        return jdbc.query("""
            SELECT p.codigo_producto,p.nombre,SUM(d.cantidad_litros) cantidad,NULL minimo,p.unidad_medida unidad
            FROM detalle_venta d JOIN venta v ON v.id_venta=d.id_venta
            JOIN producto p ON p.id_producto=d.id_producto
            JOIN categoria c ON c.id_categoria=p.id_categoria
            WHERE v.fecha_hora >= ? AND v.fecha_hora < ? AND v.estado='Confirmada'
              AND (? IS NULL OR v.id_usuario=?)
              AND c.nombre IN ('Gasolinas','Diésel','Gases vehiculares')
            GROUP BY p.id_producto,p.codigo_producto,p.nombre,p.unidad_medida
            ORDER BY cantidad DESC,p.nombre,p.id_producto
            """, COMBUSTIBLE, desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay(), vendedor, vendedor);
    }

    @Override
    public List<Dia> dias(LocalDate desde, LocalDate hasta, Long vendedor, boolean ventas, boolean finanzas) {
        return jdbc.query("""
            WITH RECURSIVE fechas AS (
                SELECT CAST(? AS DATE) AS fecha
                UNION ALL SELECT fecha + INTERVAL 1 DAY FROM fechas WHERE fecha < ?
            ), ventas_diarias AS (
                SELECT DATE(v.fecha_hora) fecha, SUM(v.total) total, COUNT(*) cantidad, SUM(d.cantidad) cantidad_vol
                FROM venta v JOIN (
                    SELECT d.id_venta, SUM(d.cantidad_litros) cantidad
                    FROM detalle_venta d
                    JOIN producto p ON p.id_producto=d.id_producto
                    WHERE p.unidad_medida='L'
                    GROUP BY d.id_venta
                ) d ON d.id_venta=v.id_venta
                WHERE ? AND v.fecha_hora >= ? AND v.fecha_hora < ? AND v.estado='Confirmada'
                  AND (? IS NULL OR v.id_usuario=?)
                GROUP BY DATE(v.fecha_hora)
            ), caja AS (
                SELECT c.fecha,c.estado,c.saldo_cierre,c.saldo_apertura,
                       SUM(CASE m.tipo WHEN 'INGRESO' THEN m.monto ELSE 0 END) ingresos,
                       SUM(CASE m.tipo WHEN 'EGRESO' THEN m.monto ELSE 0 END) egresos
                FROM caja_diaria c LEFT JOIN movimiento_caja m ON m.id_caja=c.id_caja
                WHERE ? AND c.fecha BETWEEN ? AND ?
                GROUP BY c.fecha,c.estado,c.saldo_cierre,c.saldo_apertura
            )
            SELECT f.fecha, COALESCE(v.total,0) ventas, COALESCE(v.cantidad_vol,0) litros,
                   COALESCE(v.cantidad,0) cantidad, COALESCE(c.ingresos,0) ingresos, COALESCE(c.egresos,0) egresos,
                   CASE WHEN c.estado='CERRADA' THEN c.saldo_cierre END saldo_cierre,
                   CASE WHEN c.estado='ABIERTA' THEN c.saldo_apertura+c.ingresos-c.egresos END saldo_actual,
                   COALESCE(c.estado='ABIERTA',FALSE) abierta, c.fecha IS NOT NULL tiene_caja
            FROM fechas f LEFT JOIN ventas_diarias v ON v.fecha=f.fecha
            LEFT JOIN caja c ON c.fecha=f.fecha ORDER BY f.fecha
            """, (rs, i) -> new Dia(rs.getObject("fecha", LocalDate.class), rs.getBigDecimal("ventas"),
            rs.getBigDecimal("litros"), rs.getLong("cantidad"), rs.getBigDecimal("ingresos"),
            rs.getBigDecimal("egresos"), rs.getBigDecimal("saldo_cierre"), rs.getBigDecimal("saldo_actual"),
            rs.getBoolean("abierta"), rs.getBoolean("tiene_caja")), desde, hasta, ventas,
            desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay(), vendedor, vendedor, finanzas, desde, hasta);
    }

    @Override
    public List<ProveedorCompra> comprasPorProveedor(LocalDate desde, LocalDate hasta) {
        return jdbc.query("""
            SELECT p.ruc, p.razon_social, COUNT(*) compras, COALESCE(SUM(c.total),0) total
            FROM compra c JOIN proveedor p ON p.id_proveedor=c.id_proveedor
            WHERE c.fecha_hora >= ? AND c.fecha_hora < ? AND c.estado='Confirmada'
            GROUP BY p.id_proveedor, p.ruc, p.razon_social
            ORDER BY total DESC, p.razon_social
            """, (rs, i) -> new ProveedorCompra(rs.getString("ruc"), rs.getString("razon_social"),
                rs.getLong("compras"), rs.getBigDecimal("total")),
            desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay());
    }

    @Override
    public long movimientosInventario(LocalDate desde, LocalDate hasta) {
        Long total = jdbc.queryForObject("""
            SELECT COUNT(*) FROM movimiento_inventario
            WHERE fecha_hora >= ? AND fecha_hora < ?
            """, Long.class, desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay());
        return total == null ? 0L : total;
    }
}
