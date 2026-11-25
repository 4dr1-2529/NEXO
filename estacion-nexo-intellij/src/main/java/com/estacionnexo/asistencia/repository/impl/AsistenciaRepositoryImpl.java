package com.estacionnexo.asistencia.repository.impl;

import com.estacionnexo.asistencia.model.Jornada;
import com.estacionnexo.asistencia.model.ResumenAsistencia;
import com.estacionnexo.asistencia.repository.AsistenciaRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AsistenciaRepositoryImpl implements AsistenciaRepository {
    private final JdbcTemplate jdbc;

    public AsistenciaRepositoryImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void entrada(long empleado, LocalDate fecha, LocalTime hora) {
        jdbc.update("""
            INSERT INTO asistencia (id_empleado,fecha,hora_entrada,estado) VALUES (?,?,?,'PRESENTE')
            """, empleado, fecha, hora);
    }

    @Override
    public boolean salida(long empleado, LocalDate fecha, LocalTime hora) {
        return jdbc.update("""
            UPDATE asistencia SET hora_salida=?
            WHERE id_empleado=? AND fecha=? AND estado='PRESENTE' AND hora_salida IS NULL AND hora_entrada<?
            """, hora, empleado, fecha, hora) == 1;
    }

    @Override
    public List<Jornada> consultar(Long empleado, LocalDate desde, LocalDate hasta, int limite, long inicio) {
        return jdbc.query("""
            SELECT a.id_asistencia, a.id_empleado, CONCAT(e.nombres,' ',e.apellidos) AS nombre,
                   a.fecha, a.hora_entrada, a.hora_salida, a.estado
            FROM asistencia a JOIN empleado e ON e.id_empleado=a.id_empleado
            WHERE a.fecha BETWEEN ? AND ? AND (? IS NULL OR a.id_empleado=?)
            ORDER BY a.fecha DESC, a.id_asistencia DESC LIMIT ? OFFSET ?
            """, (rs, fila) -> new Jornada(rs.getLong("id_asistencia"), rs.getLong("id_empleado"),
            rs.getString("nombre"), rs.getObject("fecha", LocalDate.class),
            rs.getObject("hora_entrada", LocalTime.class), rs.getObject("hora_salida", LocalTime.class),
            rs.getString("estado")), desde, hasta, empleado, empleado, limite, inicio);
    }

    @Override
    public ResumenAsistencia resumen(Long empleado, LocalDate desde, LocalDate hasta) {
        return jdbc.queryForObject("""
            SELECT COUNT(*) presentes, COUNT(hora_salida) completas,
                   COALESCE(SUM(TIME_TO_SEC(TIMEDIFF(hora_salida,hora_entrada))),0) segundos
            FROM asistencia
            WHERE (? IS NULL OR id_empleado=?) AND fecha BETWEEN ? AND ? AND estado='PRESENTE'
            """, (rs, i) -> new ResumenAsistencia(rs.getLong("presentes"), rs.getLong("completas"),
                rs.getLong("segundos")), empleado, empleado, desde, hasta);
    }
}
