package com.estacionnexo.shared.consulta.repository.impl;

import com.estacionnexo.shared.consulta.model.Detalle;
import com.estacionnexo.shared.consulta.model.Registro;
import com.estacionnexo.shared.consulta.repository.ConsultaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ConsultaRepositoryImpl implements ConsultaRepository {
    private static final RowMapper<Registro> MAPA = (rs, i) -> new Registro(rs.getLong("id"),
        rs.getString("codigo"), rs.getObject("fecha", LocalDateTime.class), rs.getString("responsable"),
        rs.getString("referencia"), rs.getString("estado"), rs.getBigDecimal("valor"));
    private final JdbcTemplate jdbc;

    public ConsultaRepositoryImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Registro> listar(Tipo tipo, LocalDateTime desde, LocalDateTime hasta, Long usuario,
                                 String filtro, int limite, long inicio) {
        return jdbc.query("SELECT * FROM (" + consulta(tipo) + """
            ) r WHERE fecha >= ? AND fecha < ? AND (? IS NULL OR usuario=?) AND (? IS NULL OR estado=?)
            ORDER BY fecha DESC, id DESC LIMIT ? OFFSET ?
            """, MAPA, desde, hasta, usuario, usuario, filtro, filtro, limite, inicio);
    }

    @Override
    public Optional<Registro> buscar(Tipo tipo, long id, Long usuario) {
        return jdbc.query("SELECT * FROM (" + consulta(tipo) + ") r WHERE id=? AND (? IS NULL OR usuario=?)",
            MAPA, id, usuario, usuario).stream().findFirst();
    }

    @Override
    public List<Detalle> detalles(Tipo tipo, long id) {
        String sql = switch (tipo) {
            case COMPRAS -> """
                SELECT p.nombre, d.cantidad_litros, d.precio_compra precio, d.subtotal
                FROM detalle_compra d JOIN producto p ON p.id_producto=d.id_producto
                WHERE d.id_compra=? ORDER BY d.id_detalle_compra
                """;
            case VENTAS -> """
                SELECT p.nombre, d.cantidad_litros, d.precio_unitario precio, d.subtotal
                FROM detalle_venta d JOIN producto p ON p.id_producto=d.id_producto
                WHERE d.id_venta=? ORDER BY d.id_detalle_venta
                """;
            default -> throw new IllegalArgumentException("La consulta no admite líneas de combustible.");
        };
        return jdbc.query(sql, (rs, i) -> new Detalle(rs.getString("nombre"), rs.getBigDecimal("cantidad_litros"),
            rs.getBigDecimal("precio"), rs.getBigDecimal("subtotal")), id);
    }

    private String consulta(Tipo tipo) {
        return switch (tipo) {
            case COMPRAS -> """
                SELECT c.id_compra id, c.codigo_compra codigo, c.fecha_hora fecha, c.id_usuario usuario,
                       u.username responsable, p.razon_social referencia, c.estado, c.total valor
                FROM compra c JOIN proveedor p ON p.id_proveedor=c.id_proveedor
                JOIN usuario u ON u.id_usuario=c.id_usuario
                """;
            case VENTAS -> """
                SELECT v.id_venta id, v.codigo_venta codigo, v.fecha_hora fecha, v.id_usuario usuario,
                       u.username responsable, NULL referencia, v.estado, v.total valor
                FROM venta v JOIN usuario u ON u.id_usuario=v.id_usuario
                """;
            case INVENTARIO -> """
                SELECT m.id_movimiento_inventario id, p.codigo_producto codigo, m.fecha_hora fecha,
                       m.id_usuario usuario, u.username responsable, CONCAT(p.nombre,' · ',m.motivo) referencia,
                       m.tipo estado, m.cantidad_litros valor
                FROM movimiento_inventario m JOIN producto p ON p.id_producto=m.id_producto
                JOIN usuario u ON u.id_usuario=m.id_usuario
                """;
            case CAJA -> """
                SELECT m.id_movimiento_caja id, m.codigo_movimiento codigo, m.fecha_hora fecha,
                       m.id_usuario usuario, u.username responsable,
                       CONCAT(x.nombre,' · ',COALESCE(c.codigo_compra,v.codigo_venta)) referencia,
                       m.tipo estado, m.monto valor
                FROM movimiento_caja m JOIN usuario u ON u.id_usuario=m.id_usuario
                JOIN concepto_movimiento x ON x.id_concepto=m.id_concepto
                LEFT JOIN compra c ON c.id_compra=m.id_compra
                LEFT JOIN venta v ON v.id_venta=m.id_venta
                """;
        };
    }
}
