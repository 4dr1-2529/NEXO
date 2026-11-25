package com.estacionnexo.inventario.service.impl;

import com.estacionnexo.auditoria.AuditoriaContexto;
import com.estacionnexo.exception.ReglaIncumplida;
import com.estacionnexo.inventario.repository.InventarioRepository;
import com.estacionnexo.inventario.service.InventarioService;
import com.estacionnexo.shared.Importes;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventarioServiceImpl implements InventarioService {
    private final InventarioRepository inventario;
    private final Clock reloj;
    private final AuditoriaContexto auditoria;

    public InventarioServiceImpl(InventarioRepository inventario, Clock reloj, AuditoriaContexto auditoria) {
        this.inventario = inventario;
        this.reloj = reloj;
        this.auditoria = auditoria;
    }

    @Override
    @Transactional
    public void ajustar(long usuario, long producto, Tipo tipo, BigDecimal cantidad,
                        String motivo, UUID solicitud) {
        var litros = Importes.litros(cantidad);
        if (tipo == null || solicitud == null || motivo == null || motivo.isBlank() || motivo.strip().length() > 200) {
            throw new ReglaIncumplida("Indique el tipo de ajuste y un motivo de hasta 200 caracteres.");
        }
        var existencia = inventario.bloquearProducto(producto).filter(item -> item.activo())
            .orElseThrow(() -> new ReglaIncumplida("El combustible no está disponible."));
        if (!inventario.cambiarStock(existencia.producto(), tipo == Tipo.ENTRADA ? litros : litros.negate())) {
            throw new ReglaIncumplida("El ajuste supera las existencias disponibles o la capacidad admitida.");
        }
        inventario.registrar(producto, usuario, null, null, tipo.name(), litros,
            LocalDateTime.now(reloj), motivo.strip(), solicitud.toString());
        auditoria.ok("AJUSTE_" + tipo.name(), "INVENTARIO", String.valueOf(producto),
            "stock=" + existencia.litros(), "delta=" + litros + ";motivo=" + motivo.strip());
    }
}
