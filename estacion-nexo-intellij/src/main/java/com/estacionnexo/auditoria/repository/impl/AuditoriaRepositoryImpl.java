package com.estacionnexo.auditoria.repository.impl;

import com.estacionnexo.auditoria.model.EventoBitacora;
import com.estacionnexo.auditoria.repository.AuditoriaRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class AuditoriaRepositoryImpl implements AuditoriaRepository {
    private static final RowMapper<EventoBitacora> MAPA = (rs, i) -> new EventoBitacora(
        rs.getLong("id_bitacora"), rs.getString("username"), rs.getString("rol"),
        rs.getString("accion"), rs.getString("modulo"), rs.getString("registro_afectado"),
        rs.getString("valores_anteriores"), rs.getString("valores_nuevos"),
        rs.getString("resultado"), rs.getObject("fecha_hora", LocalDateTime.class));
    private final JdbcTemplate jdbc;

    public AuditoriaRepositoryImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void insertar(String username, String rol, String accion, String modulo, String registro,
                         String anteriores, String nuevos, String resultado, LocalDateTime fecha) {
        jdbc.update("""
            INSERT INTO bitacora
                (username,rol,accion,modulo,registro_afectado,valores_anteriores,valores_nuevos,resultado,fecha_hora)
            VALUES (?,?,?,?,?,?,?,?,?)
            """, recortar(username, 30), recortar(rol, 20), recortar(accion, 40), recortar(modulo, 40),
            recortar(registro, 80), recortar(anteriores, 1000), recortar(nuevos, 1000),
            recortar(resultado, 20), fecha);
    }

    @Override
    public List<EventoBitacora> listar(String modulo, String username, LocalDateTime desde, LocalDateTime hasta,
                                       int limite, long inicio) {
        return jdbc.query("""
            SELECT * FROM bitacora
            WHERE (? IS NULL OR modulo=?) AND (? IS NULL OR username=?)
              AND fecha_hora >= ? AND fecha_hora < ?
            ORDER BY fecha_hora DESC, id_bitacora DESC
            LIMIT ? OFFSET ?
            """, MAPA, modulo, modulo, username, username, desde, hasta, limite, inicio);
    }

    private static String recortar(String valor, int max) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.strip();
        return limpio.length() <= max ? limpio : limpio.substring(0, max);
    }
}
