package com.estacionnexo.auditoria.service.impl;

import com.estacionnexo.auditoria.model.EventoBitacora;
import com.estacionnexo.auditoria.repository.AuditoriaRepository;
import com.estacionnexo.auditoria.service.AuditoriaService;
import com.estacionnexo.exception.ReglaIncumplida;
import com.estacionnexo.shared.Periodo;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditoriaServiceImpl implements AuditoriaService {
    private final AuditoriaRepository bitacora;
    private final Clock reloj;

    public AuditoriaServiceImpl(AuditoriaRepository bitacora, Clock reloj) {
        this.bitacora = bitacora;
        this.reloj = reloj;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(String username, String rol, String accion, String modulo, String registro,
                          String anteriores, String nuevos, String resultado) {
        bitacora.insertar(username, rol, accion, modulo, registro, anteriores, nuevos,
            resultado == null ? "OK" : resultado, LocalDateTime.now(reloj));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventoBitacora> listar(String modulo, String username, LocalDate desde, LocalDate hasta, int pagina) {
        if (pagina < 0) {
            throw new ReglaIncumplida("La página debe ser igual o mayor que cero.");
        }
        var periodo = Periodo.resolver(reloj, desde, hasta);
        return bitacora.listar(blankToNull(modulo), blankToNull(username),
            periodo.desde().atStartOfDay(), periodo.hasta().plusDays(1).atStartOfDay(),
            25, pagina * 25L);
    }

    private static String blankToNull(String valor) {
        return valor == null || valor.isBlank() ? null : valor.strip();
    }
}
