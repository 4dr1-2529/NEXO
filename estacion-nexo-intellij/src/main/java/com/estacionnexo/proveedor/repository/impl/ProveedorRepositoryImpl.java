package com.estacionnexo.proveedor.repository.impl;

import com.estacionnexo.proveedor.repository.ProveedorRepository;
import com.estacionnexo.proveedor.dto.ProveedorDatos;
import com.estacionnexo.proveedor.model.Proveedor;
import com.estacionnexo.shared.JdbcOperaciones;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ProveedorRepositoryImpl implements ProveedorRepository {
    private static final RowMapper<Proveedor> MAPA = (rs, i) ->
        new Proveedor(rs.getLong("id_proveedor"), new ProveedorDatos(rs.getString("ruc"), rs.getString("razon_social"), rs.getString("contacto"), rs.getString("telefono"), rs.getString("correo"), rs.getString("direccion")),
            "Activo".equals(rs.getString("estado")));
    private final JdbcTemplate jdbc;
    private final JdbcOperaciones operaciones;

    public ProveedorRepositoryImpl(JdbcTemplate jdbc, JdbcOperaciones operaciones) {
        this.jdbc = jdbc;
        this.operaciones = operaciones;
    }

    @Override
    public List<Proveedor> listar(boolean incluirInactivos, int limite, long inicio, Long categoria) {
        return jdbc.query("""
            SELECT t.* FROM proveedor t WHERE (? OR t.estado='Activo')
            ORDER BY t.razon_social, t.id_proveedor LIMIT ? OFFSET ?
            """, MAPA, incluirInactivos, limite, inicio);
    }

    @Override
    public Optional<Proveedor> buscar(long id, boolean incluirInactivos) {
        return jdbc.query("""
            SELECT t.* FROM proveedor t WHERE t.id_proveedor=? AND (? OR t.estado='Activo')
            """, MAPA, id, incluirInactivos).stream().findFirst();
    }

    @Override
    public long crear(ProveedorDatos datos) {
        return operaciones.insertar("INSERT INTO proveedor (ruc,razon_social,contacto,telefono,correo,direccion) VALUES (?,?,?,?,?,?)",
            datos.ruc(), datos.razonSocial().strip(), datos.contacto(), datos.telefono(), datos.correo(), datos.direccion());
    }

    @Override
    public boolean editar(long id, ProveedorDatos datos) {
        return jdbc.update("UPDATE proveedor SET ruc=?, razon_social=?, contacto=?, telefono=?, correo=?, direccion=? WHERE id_proveedor=?",
            datos.ruc(), datos.razonSocial().strip(), datos.contacto(), datos.telefono(), datos.correo(), datos.direccion(), id) == 1;
    }

    @Override
    public boolean cambiarEstado(long id, boolean activo) {
        return jdbc.update("UPDATE proveedor SET estado=? WHERE id_proveedor=?", activo ? "Activo" : "Inactivo", id) == 1;
    }
}
