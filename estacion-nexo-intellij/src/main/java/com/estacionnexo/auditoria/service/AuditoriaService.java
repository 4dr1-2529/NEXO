package com.estacionnexo.auditoria.service;

import com.estacionnexo.auditoria.model.EventoBitacora;
import java.time.LocalDate;
import java.util.List;

public interface AuditoriaService {
    void registrar(String username, String rol, String accion, String modulo, String registro,
                   String anteriores, String nuevos, String resultado);
    List<EventoBitacora> listar(String modulo, String username, LocalDate desde, LocalDate hasta, int pagina);
}
