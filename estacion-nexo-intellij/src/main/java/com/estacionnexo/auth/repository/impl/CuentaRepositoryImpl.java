package com.estacionnexo.auth.repository.impl;

import com.estacionnexo.auth.model.Cuenta;
import com.estacionnexo.auth.repository.CuentaRepository;
import com.estacionnexo.shared.JdbcOperaciones;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CuentaRepositoryImpl implements CuentaRepository {
    private final JdbcTemplate jdbc;
    private final JdbcOperaciones operaciones;

    public CuentaRepositoryImpl(JdbcTemplate jdbc, JdbcOperaciones operaciones) {
        this.jdbc = jdbc;
        this.operaciones = operaciones;
    }

    @Override
    public Optional<Cuenta> buscar(String username) {
        return jdbc.query("""
            SELECT u.id_usuario, e.id_empleado, u.username, u.password_hash, r.codigo,
                   CONCAT(e.nombres,' ', e.apellidos) AS nombre,
                   (u.estado='Activo' AND e.estado='Activo' AND r.estado='Activo') AS activa
            FROM usuario u JOIN empleado e ON e.id_empleado=u.id_empleado
            JOIN rol r ON r.id_rol=u.id_rol WHERE u.username=?
            """, (rs, fila) -> new Cuenta(rs.getLong("id_usuario"), rs.getLong("id_empleado"),
            rs.getString("username"), rs.getString("password_hash"), rs.getString("codigo"),
            rs.getString("nombre"), rs.getBoolean("activa")), username).stream().findFirst();
    }

    @Override
    public void registrarAcceso(long usuario, LocalDateTime fecha) {
        jdbc.update("UPDATE usuario SET ultimo_acceso=? WHERE id_usuario=?", fecha, usuario);
    }

    @Override
    public void crearPrimerAdministrador(String username, String hash, String dni, String nombres,
                                         String apellidos, String telefono, LocalDateTime fecha) {
        long rol = jdbc.queryForObject("SELECT id_rol FROM rol WHERE codigo='ADMINISTRADOR' FOR UPDATE", Long.class);
        if (jdbc.queryForObject("SELECT COUNT(*) FROM usuario WHERE id_rol=?", Long.class, rol) > 0) {
            return;
        }
        long empleado = operaciones.insertar("""
            INSERT INTO empleado (dni,nombres,apellidos,cargo,telefono,fecha_ingreso,estado)
            VALUES (?,?,?,'Administrador',?,?,'Activo')
            """, dni, nombres, apellidos, telefono, fecha.toLocalDate());
        jdbc.update("""
            INSERT INTO usuario (id_empleado,id_rol,username,password_hash,estado,fecha_creacion)
            VALUES (?,?,?,?,'Activo',?)
            """, empleado, rol, username, hash, fecha);
    }
}
