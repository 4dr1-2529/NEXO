package com.estacionnexo.shared;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import java.sql.Statement;
import java.util.Objects;

@Component
public class JdbcOperaciones {
    private final JdbcTemplate jdbc;

    public JdbcOperaciones(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long insertar(String sql, Object... argumentos) {
        var claves = new GeneratedKeyHolder();
        jdbc.update(conexion -> {
            var sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < argumentos.length; i++) {
                sentencia.setObject(i + 1, argumentos[i]);
            }
            return sentencia;
        }, claves);
        return Objects.requireNonNull(claves.getKey()).longValue();
    }
}
