package com.estacionnexo.asistencia.service;

import com.estacionnexo.asistencia.model.Jornada;
import com.estacionnexo.asistencia.model.ResumenAsistencia;
import java.time.LocalDate;
import java.util.List;

public interface AsistenciaService {
    enum Marca { ENTRADA, SALIDA }
    void marcar(String username, Marca marca);
    List<Jornada> propia(String username, LocalDate desde, LocalDate hasta, int pagina);
    ResumenAsistencia resumen(String username, LocalDate desde, LocalDate hasta);
    ResumenAsistencia resumenPersonal(String username, Long empleado, LocalDate desde, LocalDate hasta);
    List<Jornada> personal(String username, Long empleado, LocalDate desde, LocalDate hasta, int pagina);
}
