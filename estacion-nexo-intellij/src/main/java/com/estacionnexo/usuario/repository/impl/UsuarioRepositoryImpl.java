package com.estacionnexo.usuario.repository.impl;

import com.estacionnexo.shared.JdbcOperaciones;
import com.estacionnexo.usuario.dto.UsuarioDatos;
import com.estacionnexo.usuario.model.Rol;
import com.estacionnexo.usuario.model.Usuario;
import com.estacionnexo.usuario.repository.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class UsuarioRepositoryImpl implements UsuarioRepository {
    private static final String USUARIOS = """
        SELECT u.id_usuario,u.id_empleado,u.id_rol,u.username,u.estado,u.ultimo_acceso,
               CONCAT(e.nombres,' ',e.apellidos) nombre,r.nombre perfil
        FROM usuario u JOIN empleado e ON e.id_empleado=u.id_empleado JOIN rol r ON r.id_rol=u.id_rol
        """;
    private static final RowMapper<Usuario> MAPA = (rs, i) -> new Usuario(rs.getLong("id_usuario"),
        rs.getLong("id_empleado"), rs.getLong("id_rol"), rs.getString("username"), rs.getString("nombre"),
        rs.getString("perfil"), "Activo".equals(rs.getString("estado")),
        rs.getObject("ultimo_acceso", LocalDateTime.class));
    private final JdbcTemplate jdbc;
    private final JdbcOperaciones operaciones;

    public UsuarioRepositoryImpl(JdbcTemplate jdbc, JdbcOperaciones operaciones) {
        this.jdbc = jdbc;
        this.operaciones = operaciones;
    }

    @Override
    public List<Usuario> listar(int limite, long inicio) {
        return jdbc.query(USUARIOS + " ORDER BY u.username LIMIT ? OFFSET ?", MAPA, limite, inicio);
    }

    @Override
    public Optional<Usuario> buscar(long id) {
        return jdbc.query(USUARIOS + " WHERE u.id_usuario=?", MAPA, id).stream().findFirst();
    }

    @Override
    public List<Rol> roles() {
        return jdbc.query("SELECT id_rol,codigo,nombre FROM rol WHERE estado='Activo' ORDER BY nombre",
            (rs, i) -> new Rol(rs.getLong("id_rol"), rs.getString("codigo"), rs.getString("nombre")));
    }

    @Override
    public boolean empleadoActivo(long id) {
        return !jdbc.queryForList("SELECT id_empleado FROM empleado WHERE id_empleado=? AND estado='Activo' FOR UPDATE",
            Long.class, id).isEmpty();
    }

    @Override
    public long crear(UsuarioDatos datos, String hash, LocalDateTime fecha) {
        return operaciones.insertar("""
            INSERT INTO usuario (id_empleado,id_rol,username,password_hash,fecha_creacion) VALUES (?,?,?,?,?)
            """, datos.empleado(), datos.rol(), datos.username(), hash, fecha);
    }

    @Override
    public void editar(long id, UsuarioDatos datos, String hash) {
        jdbc.update("UPDATE usuario SET id_rol=?, username=?, password_hash=COALESCE(?,password_hash) WHERE id_usuario=?",
            datos.rol(), datos.username(), hash, id);
    }

    @Override
    public void estado(long id, boolean activo) {
        jdbc.update("UPDATE usuario SET estado=? WHERE id_usuario=?", activo ? "Activo" : "Inactivo", id);
    }
}
