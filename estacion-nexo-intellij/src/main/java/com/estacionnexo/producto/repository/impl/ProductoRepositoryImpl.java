package com.estacionnexo.producto.repository.impl;

import com.estacionnexo.producto.repository.ProductoRepository;
import com.estacionnexo.producto.dto.ProductoDatos;
import com.estacionnexo.producto.model.Producto;
import com.estacionnexo.shared.JdbcOperaciones;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ProductoRepositoryImpl implements ProductoRepository {
    private static final RowMapper<Producto> MAPA = (rs, i) ->
        new Producto(rs.getLong("id_producto"),
            new ProductoDatos(rs.getString("codigo_producto"), rs.getLong("id_categoria"), rs.getString("nombre"),
                rs.getBigDecimal("precio_venta"), rs.getBigDecimal("stock_minimo"), rs.getString("unidad_medida")),
            "Activo".equals(rs.getString("estado")), rs.getBigDecimal("stock_actual"));
    private final JdbcTemplate jdbc;
    private final JdbcOperaciones operaciones;

    public ProductoRepositoryImpl(JdbcTemplate jdbc, JdbcOperaciones operaciones) {
        this.jdbc = jdbc;
        this.operaciones = operaciones;
    }

    @Override
    public List<Producto> listar(boolean incluirInactivos, int limite, long inicio, Long categoria) {
        return jdbc.query("""
            SELECT t.* FROM producto t JOIN categoria c ON c.id_categoria=t.id_categoria
            WHERE (? OR (t.estado='Activo' AND c.estado='Activo')) AND (? IS NULL OR t.id_categoria=?)
            ORDER BY t.nombre, t.id_producto LIMIT ? OFFSET ?
            """, MAPA, incluirInactivos, categoria, categoria, limite, inicio);
    }

    @Override
    public Optional<Producto> buscar(long id, boolean incluirInactivos) {
        return jdbc.query("""
            SELECT t.* FROM producto t JOIN categoria c ON c.id_categoria=t.id_categoria
            WHERE t.id_producto=? AND (? OR (t.estado='Activo' AND c.estado='Activo'))
            """, MAPA, id, incluirInactivos).stream().findFirst();
    }

    @Override
    public Optional<Producto> buscarPorCodigo(String codigo, boolean incluirInactivos) {
        return jdbc.query("""
            SELECT t.* FROM producto t JOIN categoria c ON c.id_categoria=t.id_categoria
            WHERE t.codigo_producto=? AND (? OR (t.estado='Activo' AND c.estado='Activo'))
            """, MAPA, codigo, incluirInactivos).stream().findFirst();
    }

    @Override
    public long crear(ProductoDatos datos) {
        return operaciones.insertar("""
            INSERT INTO producto (codigo_producto,id_categoria,nombre,unidad_medida,precio_venta,stock_minimo)
            VALUES (?,?,?,?,?,?)
            """, datos.codigo().strip(), datos.categoria(), datos.nombre().strip(), datos.unidadMedida(),
            datos.precio(), datos.stockMinimo());
    }

    @Override
    public boolean editar(long id, ProductoDatos datos) {
        return jdbc.update("""
            UPDATE producto SET codigo_producto=?, id_categoria=?, nombre=?, unidad_medida=?,
            precio_venta=?, stock_minimo=? WHERE id_producto=?
            """, datos.codigo().strip(), datos.categoria(), datos.nombre().strip(), datos.unidadMedida(),
            datos.precio(), datos.stockMinimo(), id) == 1;
    }

    @Override
    public boolean sincronizarMetadatos(String codigo, long categoria, String nombre, String unidad) {
        return jdbc.update("""
            UPDATE producto SET id_categoria=?, nombre=?, unidad_medida=?
            WHERE codigo_producto=?
            """, categoria, nombre, unidad, codigo) == 1;
    }

    @Override
    public boolean cambiarEstado(long id, boolean activo) {
        return jdbc.update("UPDATE producto SET estado=? WHERE id_producto=?", activo ? "Activo" : "Inactivo", id) == 1;
    }
}
