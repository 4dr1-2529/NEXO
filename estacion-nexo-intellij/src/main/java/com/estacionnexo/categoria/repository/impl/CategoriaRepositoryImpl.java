package com.estacionnexo.categoria.repository.impl;

import com.estacionnexo.categoria.repository.CategoriaRepository;
import com.estacionnexo.categoria.dto.CategoriaDatos;
import com.estacionnexo.categoria.model.Categoria;
import com.estacionnexo.shared.JdbcOperaciones;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class CategoriaRepositoryImpl implements CategoriaRepository {
    private static final RowMapper<Categoria> MAPA = (rs, i) ->
        new Categoria(rs.getLong("id_categoria"), new CategoriaDatos(rs.getString("nombre"), rs.getString("descripcion")),
            "Activo".equals(rs.getString("estado")));
    private final JdbcTemplate jdbc;
    private final JdbcOperaciones operaciones;

    public CategoriaRepositoryImpl(JdbcTemplate jdbc, JdbcOperaciones operaciones) {
        this.jdbc = jdbc;
        this.operaciones = operaciones;
    }

    @Override
    public List<Categoria> listar(boolean incluirInactivos, int limite, long inicio, Long categoria) {
        return jdbc.query("""
            SELECT t.* FROM categoria t WHERE (? OR t.estado='Activo')
            ORDER BY t.nombre, t.id_categoria LIMIT ? OFFSET ?
            """, MAPA, incluirInactivos, limite, inicio);
    }

    @Override
    public Optional<Categoria> buscar(long id, boolean incluirInactivos) {
        return jdbc.query("""
            SELECT t.* FROM categoria t WHERE t.id_categoria=? AND (? OR t.estado='Activo')
            """, MAPA, id, incluirInactivos).stream().findFirst();
    }

    @Override
    public Optional<Categoria> buscarPorNombre(String nombre, boolean incluirInactivos) {
        return jdbc.query("""
            SELECT t.* FROM categoria t WHERE t.nombre=? AND (? OR t.estado='Activo')
            """, MAPA, nombre, incluirInactivos).stream().findFirst();
    }

    @Override
    public long crear(CategoriaDatos datos, LocalDateTime fecha) {
        return operaciones.insertar("INSERT INTO categoria (nombre,descripcion,fecha_creacion) VALUES (?,?,?)",
            datos.nombre().strip(), datos.descripcion(), fecha);
    }

    @Override
    public boolean editar(long id, CategoriaDatos datos) {
        return jdbc.update("UPDATE categoria SET nombre=?, descripcion=? WHERE id_categoria=?",
            datos.nombre().strip(), datos.descripcion(), id) == 1;
    }

    @Override
    public boolean cambiarEstado(long id, boolean activo) {
        return jdbc.update("UPDATE categoria SET estado=? WHERE id_categoria=?", activo ? "Activo" : "Inactivo", id) == 1;
    }
}
