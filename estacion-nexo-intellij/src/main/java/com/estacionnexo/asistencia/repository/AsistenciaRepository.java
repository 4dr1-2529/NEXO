package com.estacionnexo.asistencia.repository;

import com.estacionnexo.asistencia.model.Jornada;
import com.estacionnexo.asistencia.model.ResumenAsistencia;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AsistenciaRepository {
    void entrada(long empleado, LocalDate fecha, LocalTime hora);
    boolean salida(long empleado, LocalDate fecha, LocalTime hora);
    List<Jornada> consultar(Long empleado, LocalDate desde, LocalDate hasta, int limite, long inicio);
    ResumenAsistencia resumen(Long empleado, LocalDate desde, LocalDate hasta);
}
