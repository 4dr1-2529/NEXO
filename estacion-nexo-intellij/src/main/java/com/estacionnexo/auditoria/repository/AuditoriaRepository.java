package com.estacionnexo.auditoria.repository;

import com.estacionnexo.auditoria.model.EventoBitacora;
import java.time.LocalDateTime;
import java.util.List;

public interface AuditoriaRepository {
    void insertar(String username, String rol, String accion, String modulo, String registro,
                  String anteriores, String nuevos, String resultado, LocalDateTime fecha);
    List<EventoBitacora> listar(String modulo, String username, LocalDateTime desde, LocalDateTime hasta,
                                int limite, long inicio);
}
