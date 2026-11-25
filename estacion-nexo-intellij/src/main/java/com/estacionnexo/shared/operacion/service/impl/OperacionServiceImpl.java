package com.estacionnexo.shared.operacion.service.impl;

import com.estacionnexo.auditoria.AuditoriaContexto;
import com.estacionnexo.compra.repository.CompraRepository;
import com.estacionnexo.exception.ReglaIncumplida;
import com.estacionnexo.finanzas.repository.CajaRepository;
import com.estacionnexo.inventario.model.Existencia;
import com.estacionnexo.inventario.repository.InventarioRepository;
import com.estacionnexo.shared.Comprobante;
import com.estacionnexo.shared.Importes;
import com.estacionnexo.shared.operacion.dto.LineaSolicitud;
import com.estacionnexo.shared.operacion.dto.OperacionSolicitud;
import com.estacionnexo.shared.operacion.service.OperacionService;
import com.estacionnexo.venta.repository.VentaRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OperacionServiceImpl implements OperacionService {
    private final CajaRepository cajas;
    private final InventarioRepository inventario;
    private final CompraRepository compras;
    private final VentaRepository ventas;
    private final Clock reloj;
    private final AuditoriaContexto auditoria;

    public OperacionServiceImpl(CajaRepository cajas, InventarioRepository inventario,
                                CompraRepository compras, VentaRepository ventas, Clock reloj,
                                AuditoriaContexto auditoria) {
        this.cajas = cajas;
        this.inventario = inventario;
        this.compras = compras;
        this.ventas = ventas;
        this.reloj = reloj;
        this.auditoria = auditoria;
    }

    @Override
    @Transactional
    public long comprar(long usuario, OperacionSolicitud solicitud) {
        return registrar(usuario, solicitud, true);
    }

    @Override
    @Transactional
    public long vender(long usuario, OperacionSolicitud solicitud) {
        return registrar(usuario, solicitud, false);
    }

    private long registrar(long usuario, OperacionSolicitud solicitud, boolean compra) {
        if (solicitud == null || solicitud.solicitud() == null || solicitud.lineas() == null
                || solicitud.lineas().isEmpty() || solicitud.lineas().size() > 50
                || (solicitud.observacion() != null && solicitud.observacion().length() > 200)) {
            throw new ReglaIncumplida("La operación debe contener entre 1 y 50 líneas válidas.");
        }
        var productos = new HashSet<Long>();
        for (var linea : solicitud.lineas()) {
            if (linea == null || linea.producto() <= 0 || !productos.add(linea.producto())) {
                throw new ReglaIncumplida("Seleccione un combustible distinto por línea.");
            }
        }
        var fecha = LocalDateTime.now(reloj);
        var caja = cajas.bloquear(fecha.toLocalDate()).filter(item -> item.abierta())
            .orElseThrow(() -> new ReglaIncumplida("Abra la caja del día antes de confirmar la operación."));
        if (compra && (solicitud.proveedor() == null || !compras.proveedorDisponible(solicitud.proveedor()))) {
            throw new ReglaIncumplida("Seleccione un proveedor activo.");
        }
        // Se adquiere primero la caja y luego los combustibles en orden de ID.
        var lineas = solicitud.lineas().stream().sorted(Comparator.comparingLong(LineaSolicitud::producto))
            .map(linea -> valorar(linea, compra)).toList();
        var total = Importes.importe(lineas.stream().map(Linea::subtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add), false);
        String token = solicitud.solicitud().toString();
        long origen = compra
            ? compras.registrar(Comprobante.codigo("C"), solicitud.proveedor(), usuario, fecha, total,
                solicitud.observacion(), token)
            : ventas.registrar(Comprobante.codigo("V"), usuario, fecha, total, token);
        for (var linea : lineas) {
            long detalle = compra
                ? compras.registrarDetalle(origen, linea.producto(), linea.litros(), linea.precio(), linea.subtotal())
                : ventas.registrarDetalle(origen, linea.producto(), linea.litros(), linea.precio(), linea.subtotal());
            if (!inventario.cambiarStock(linea.producto(), compra ? linea.litros() : linea.litros().negate())) {
                throw new ReglaIncumplida("El movimiento excede el rango permitido de existencias.");
            }
            inventario.registrar(linea.producto(), usuario, compra ? detalle : null, compra ? null : detalle,
                compra ? "ENTRADA" : "SALIDA", linea.litros(), fecha, compra ? "Compra" : "Venta", null);
        }
        cajas.registrar(caja.id(), usuario, compra ? origen : null, compra ? null : origen,
            total, fecha, Comprobante.codigo("MC"));
        auditoria.ok(compra ? "COMPRA" : "VENTA", compra ? "COMPRAS" : "VENTAS",
            String.valueOf(origen), null, "total=" + total + ";lineas=" + lineas.size());
        return origen;
    }

    private Linea valorar(LineaSolicitud linea, boolean compra) {
        var producto = inventario.bloquearProducto(linea.producto()).filter(Existencia::activo)
            .orElseThrow(() -> new ReglaIncumplida("Uno de los combustibles ya no está disponible."));
        var litros = Importes.litros(linea.litros());
        if (!compra && litros.compareTo(producto.litros()) > 0) {
            throw new ReglaIncumplida("Las existencias son insuficientes.");
        }
        var precio = Importes.importe(compra ? linea.precio() : producto.precio(), false);
        return new Linea(linea.producto(), litros, precio, Importes.subtotal(litros, precio));
    }

    private record Linea(long producto, BigDecimal litros, BigDecimal precio, BigDecimal subtotal) {}
}
