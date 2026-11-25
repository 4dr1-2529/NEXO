package com.estacionnexo.shared.consulta.service.impl;

import com.estacionnexo.auth.model.Cuenta;
import com.estacionnexo.exception.ReglaIncumplida;
import com.estacionnexo.shared.consulta.model.Registro;
import com.estacionnexo.shared.consulta.repository.ConsultaRepository;
import com.estacionnexo.shared.consulta.repository.ConsultaRepository.Tipo;
import com.estacionnexo.shared.consulta.service.ConsultaService;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsultaServiceImpl implements ConsultaService {
    private final ConsultaRepository consultas;
    private final Clock reloj;

    public ConsultaServiceImpl(ConsultaRepository consultas, Clock reloj) {
        this.consultas = consultas;
        this.reloj = reloj;
    }

    @Override
    public List<Registro> listar(Tipo tipo, Cuenta cuenta, LocalDate desde, LocalDate hasta, String filtro, int pagina) {
        Long usuario = autorizar(tipo, cuenta);
        var fin = hasta == null ? LocalDate.now(reloj) : hasta;
        var inicio = desde == null ? fin.minusDays(30) : desde;
        if (fin.isBefore(inicio) || pagina < 0) {
            throw new ReglaIncumplida("Revise las fechas y el número de página.");
        }
        return consultas.listar(tipo, inicio.atStartOfDay(), fin.plusDays(1).atStartOfDay(),
            usuario, filtro, 25, pagina * 25L);
    }

    @Override
    @Transactional(readOnly = true)
    public Resultado detalle(Tipo tipo, Cuenta cuenta, long id) {
        Long usuario = autorizar(tipo, cuenta);
        var registro = consultas.buscar(tipo, id, usuario)
            .orElseThrow(() -> new ReglaIncumplida("El movimiento solicitado no está disponible."));
        var lineas = tipo == Tipo.COMPRAS || tipo == Tipo.VENTAS ? consultas.detalles(tipo, id) : List.<com.estacionnexo.shared.consulta.model.Detalle>of();
        return new Resultado(registro, lineas);
    }

    private Long autorizar(Tipo tipo, Cuenta cuenta) {
        if (cuenta.activa() && "ADMINISTRADOR".equals(cuenta.rol())) {
            return null;
        }
        if (cuenta.activa() && tipo == Tipo.VENTAS && "VENDEDOR".equals(cuenta.rol())) {
            return cuenta.usuario();
        }
        if (cuenta.activa() && tipo == Tipo.INVENTARIO && "OPERADOR".equals(cuenta.rol())) {
            return null;
        }
        throw new AccessDeniedException("El rol no permite esta consulta.");
    }
}
