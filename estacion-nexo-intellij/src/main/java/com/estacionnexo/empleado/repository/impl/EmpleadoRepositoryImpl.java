package com.estacionnexo.empleado.repository.impl;

import com.estacionnexo.empleado.dto.EmpleadoDatos;
import com.estacionnexo.empleado.model.Empleado;
import com.estacionnexo.empleado.repository.EmpleadoRepository;
import com.estacionnexo.shared.JdbcOperaciones;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class EmpleadoRepositoryImpl implements EmpleadoRepository {
    private static final RowMapper<Empleado> MAPA = (rs, n) -> new Empleado(rs.getLong("id_empleado"),
        new EmpleadoDatos(rs.getString("dni"), rs.getString("nombres"), rs.getString("apellidos"),
            rs.getString("cargo"), rs.getString("telefono"), rs.getObject("fecha_ingreso", LocalDate.class)),
        "Activo".equals(rs.getString("estado")));
    private final JdbcTemplate jdbc;
    private final JdbcOperaciones operaciones;

    public EmpleadoRepositoryImpl(JdbcTemplate jdbc, JdbcOperaciones operaciones) {
        this.jdbc = jdbc;
        this.operaciones = operaciones;
    }

    @Override
    public List<Empleado> listar(long inicio, int limite) {
        return jdbc.query("SELECT * FROM empleado ORDER BY apellidos, nombres, id_empleado LIMIT ? OFFSET ?",
            MAPA, limite, inicio);
    }

    @Override
    public Optional<Empleado> buscar(long id) {
        return jdbc.query("SELECT * FROM empleado WHERE id_empleado=?", MAPA, id).stream().findFirst();
    }

    @Override
    public long insertar(EmpleadoDatos datos) {
        return operaciones.insertar("""
            INSERT INTO empleado (dni,nombres,apellidos,cargo,telefono,fecha_ingreso)
            VALUES (?,?,?,?,?,?)
            """, datos.dni(), datos.nombres().strip(), datos.apellidos().strip(), datos.cargo().strip(),
            datos.telefono().strip(), datos.fechaIngreso());
    }

    @Override
    public void editar(long id, EmpleadoDatos datos) {
        jdbc.update("""
            UPDATE empleado SET dni=?, nombres=?, apellidos=?, cargo=?, telefono=?, fecha_ingreso=?
            WHERE id_empleado=?
            """, datos.dni(), datos.nombres().strip(), datos.apellidos().strip(), datos.cargo().strip(),
            datos.telefono().strip(), datos.fechaIngreso(), id);
    }

    @Override
    public void estado(long id, boolean activo) {
        jdbc.update("UPDATE empleado SET estado=? WHERE id_empleado=?", activo ? "Activo" : "Inactivo", id);
        if (!activo) {
            jdbc.update("UPDATE usuario SET estado='Inactivo' WHERE id_empleado=?", id);
        }
    }
}
