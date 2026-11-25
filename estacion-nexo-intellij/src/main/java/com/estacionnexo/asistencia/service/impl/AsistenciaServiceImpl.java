package com.estacionnexo.asistencia.service.impl;

import com.estacionnexo.asistencia.model.Jornada;
import com.estacionnexo.asistencia.model.ResumenAsistencia;
import com.estacionnexo.asistencia.repository.AsistenciaRepository;
import com.estacionnexo.asistencia.service.AsistenciaService;
import com.estacionnexo.auth.model.Cuenta;
import com.estacionnexo.auth.service.CuentaService;
import com.estacionnexo.exception.ReglaIncumplida;
import com.estacionnexo.shared.Periodo;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AsistenciaServiceImpl implements AsistenciaService {
    private static final int PAGINA = 25;
    private final AsistenciaRepository asistencia;
    private final CuentaService cuentas;
    private final Clock reloj;

    public AsistenciaServiceImpl(AsistenciaRepository asistencia, CuentaService cuentas, Clock reloj) {
        this.asistencia = asistencia;
        this.cuentas = cuentas;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public void marcar(String username, Marca marca) {
        long empleado = cuentaActiva(username).empleado();
        var momento = LocalDateTime.now(reloj).truncatedTo(ChronoUnit.SECONDS);
        if (marca == Marca.ENTRADA) {
            try {
                asistencia.entrada(empleado, momento.toLocalDate(), momento.toLocalTime());
            } catch (DuplicateKeyException e) {
                throw new ReglaIncumplida("La entrada de hoy ya está registrada.");
            }
        } else if (marca != Marca.SALIDA || !asistencia.salida(empleado, momento.toLocalDate(), momento.toLocalTime())) {
            throw new ReglaIncumplida("La salida requiere una entrada anterior y una jornada sin cierre.");
        }
    }

    @Override
    public List<Jornada> propia(String username, LocalDate desde, LocalDate hasta, int pagina) {
        return consultar(cuentaActiva(username).empleado(), desde, hasta, pagina);
    }

    @Override
    public ResumenAsistencia resumen(String username, LocalDate desde, LocalDate hasta) {
        return resumen(cuentaActiva(username).empleado(), desde, hasta);
    }

    @Override
    public ResumenAsistencia resumenPersonal(String username, Long empleado, LocalDate desde, LocalDate hasta) {
        autorizarPersonal(username);
        return resumen(empleado, desde, hasta);
    }

    private ResumenAsistencia resumen(Long empleado, LocalDate desde, LocalDate hasta) {
        var periodo = Periodo.resolver(reloj, desde, hasta);
        return asistencia.resumen(empleado, periodo.desde(), periodo.hasta());
    }

    @Override
    public List<Jornada> personal(String username, Long empleado, LocalDate desde, LocalDate hasta, int pagina) {
        autorizarPersonal(username);
        return consultar(empleado, desde, hasta, pagina);
    }

    private void autorizarPersonal(String username) {
        if (!"ADMINISTRADOR".equals(cuentaActiva(username).rol())) {
            throw new AccessDeniedException("El control de personal requiere acceso de administrador.");
        }
    }

    private List<Jornada> consultar(Long empleado, LocalDate desde, LocalDate hasta, int pagina) {
        var periodo = Periodo.resolver(reloj, desde, hasta);
        if (pagina < 0) {
            throw new ReglaIncumplida("Revise el número de página.");
        }
        return asistencia.consultar(empleado, periodo.desde(), periodo.hasta(), PAGINA, (long) pagina * PAGINA);
    }

    private Cuenta cuentaActiva(String username) {
        return cuentas.buscar(username).filter(Cuenta::activa)
            .orElseThrow(() -> new AccessDeniedException("La cuenta no está activa."));
    }
}
