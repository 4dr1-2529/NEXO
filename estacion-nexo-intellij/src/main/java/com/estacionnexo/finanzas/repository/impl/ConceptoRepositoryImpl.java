package com.estacionnexo.finanzas.repository.impl;

import com.estacionnexo.finanzas.dto.ConceptoDatos;
import com.estacionnexo.finanzas.model.Concepto;
import com.estacionnexo.finanzas.repository.ConceptoRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ConceptoRepositoryImpl implements ConceptoRepository {
    private static final RowMapper<Concepto> MAPA = (rs, i) -> new Concepto(rs.getLong("id_concepto"),
        new ConceptoDatos(rs.getString("codigo"), rs.getString("nombre"),
            ConceptoDatos.Tipo.valueOf(rs.getString("tipo"))), "Activo".equals(rs.getString("estado")));
    private final JdbcTemplate jdbc;

    public ConceptoRepositoryImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Concepto> listar(int limite, long inicio) {
        return jdbc.query("SELECT * FROM concepto_movimiento ORDER BY nombre, id_concepto LIMIT ? OFFSET ?",
            MAPA, limite, inicio);
    }

    @Override
    public Optional<Concepto> buscar(long id) {
        return jdbc.query("SELECT * FROM concepto_movimiento WHERE id_concepto=?", MAPA, id).stream().findFirst();
    }

    @Override
    public void crear(ConceptoDatos datos) {
        jdbc.update("INSERT INTO concepto_movimiento (codigo,nombre,tipo) VALUES (?,?,?)",
            datos.codigo(), datos.nombre().strip(), datos.tipo().name());
    }

    @Override
    public void editar(long id, String nombre) {
        jdbc.update("UPDATE concepto_movimiento SET nombre=? WHERE id_concepto=?", nombre, id);
    }

    @Override
    public void cambiarEstado(long id, boolean activo) {
        jdbc.update("UPDATE concepto_movimiento SET estado=? WHERE id_concepto=?", activo ? "Activo" : "Inactivo", id);
    }
}
