package com.estacionnexo.finanzas.service.impl;

import com.estacionnexo.auditoria.AuditoriaContexto;
import com.estacionnexo.exception.ReglaIncumplida;
import com.estacionnexo.finanzas.model.Caja;
import com.estacionnexo.finanzas.repository.CajaRepository;
import com.estacionnexo.finanzas.repository.ConceptoRepository;
import com.estacionnexo.finanzas.service.CajaService;
import com.estacionnexo.shared.Comprobante;
import com.estacionnexo.shared.Importes;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CajaServiceImpl implements CajaService {
    private final CajaRepository cajas;
    private final ConceptoRepository conceptos;
    private final Clock reloj;
    private final AuditoriaContexto auditoria;

    public CajaServiceImpl(CajaRepository cajas, ConceptoRepository conceptos, Clock reloj,
                           AuditoriaContexto auditoria) {
        this.cajas = cajas;
        this.conceptos = conceptos;
        this.reloj = reloj;
        this.auditoria = auditoria;
    }

    @Override
    @Transactional
    public long abrir(long usuario, BigDecimal apertura) {
        try {
            long id = cajas.abrir(LocalDateTime.now(reloj), usuario, Importes.importe(apertura, true));
            auditoria.ok("APERTURA", "CAJA", String.valueOf(id), null, "apertura=" + apertura);
            return id;
        } catch (DuplicateKeyException e) {
            throw new ReglaIncumplida("Ya existe una caja para esta fecha.");
        }
    }

    @Override
    @Transactional
    public BigDecimal cerrar(long usuario) {
        var fecha = LocalDateTime.now(reloj);
        var caja = cajas.bloquear(fecha.toLocalDate()).filter(Caja::abierta)
            .orElseThrow(() -> new ReglaIncumplida("No hay una caja abierta para esta fecha."));
        var saldo = cajas.saldo(caja);
        Importes.importe(saldo.abs(), true);
        cajas.cerrar(caja, usuario, fecha, saldo);
        auditoria.ok("CIERRE", "CAJA", String.valueOf(caja.id()), null, "saldo=" + saldo);
        return saldo;
    }

    @Override
    public Optional<Caja> actual() {
        return cajas.buscar(LocalDate.now(reloj));
    }

    @Override
    @Transactional
    public void movimientoManual(long usuario, long conceptoId, BigDecimal monto, String descripcion, UUID solicitud) {
        var concepto = conceptos.buscar(conceptoId)
            .orElseThrow(() -> new ReglaIncumplida("Concepto no encontrado."));
        if (!concepto.activo()) {
            throw new ReglaIncumplida("El concepto económico está desactivado.");
        }
        String codigoConcepto = concepto.datos().codigo();
        if ("COMPRA".equals(codigoConcepto) || "VENTA".equals(codigoConcepto)) {
            throw new ReglaIncumplida("Use compras o ventas para esos conceptos.");
        }
        var fecha = LocalDateTime.now(reloj);
        var caja = cajas.bloquear(fecha.toLocalDate()).filter(Caja::abierta)
            .orElseThrow(() -> new ReglaIncumplida("Debe haber una caja abierta para registrar el movimiento."));
        String texto = descripcion == null || descripcion.isBlank()
            ? concepto.datos().nombre() : descripcion.strip();
        if (texto.length() > 200) {
            throw new ReglaIncumplida("La descripción no puede superar 200 caracteres.");
        }
        try {
            cajas.registrarManual(caja.id(), usuario, concepto.id(), concepto.datos().tipo().name(),
                Importes.importe(monto, false), texto, fecha, Comprobante.codigo("MM"), solicitud.toString());
            auditoria.ok("MOVIMIENTO_MANUAL", "CAJA", concepto.datos().codigo(), null,
                "tipo=" + concepto.datos().tipo() + ";monto=" + monto);
        } catch (DuplicateKeyException e) {
            throw new ReglaIncumplida("Esta solicitud ya fue registrada.");
        }
    }
}
