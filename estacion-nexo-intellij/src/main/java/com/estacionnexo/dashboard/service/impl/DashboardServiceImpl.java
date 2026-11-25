package com.estacionnexo.dashboard.service.impl;

import com.estacionnexo.auth.model.Cuenta;
import com.estacionnexo.dashboard.model.CombustiblePanel;
import com.estacionnexo.dashboard.model.Dia;
import com.estacionnexo.dashboard.model.Grafico;
import com.estacionnexo.dashboard.model.ProveedorCompra;
import com.estacionnexo.dashboard.repository.DashboardRepository;
import com.estacionnexo.dashboard.service.DashboardService;
import com.estacionnexo.shared.Periodo;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardServiceImpl implements DashboardService {
    private final DashboardRepository datos;
    private final Clock reloj;

    public DashboardServiceImpl(DashboardRepository datos, Clock reloj) {
        this.datos = datos;
        this.reloj = reloj;
    }

    @Override
    @Transactional(readOnly = true)
    public Resumen cargar(Cuenta cuenta, LocalDate desde, LocalDate hasta) {
        var periodo = Periodo.resolver(reloj, desde, hasta);
        boolean admin = "ADMINISTRADOR".equals(cuenta.rol());
        boolean vendedor = "VENDEDOR".equals(cuenta.rol());
        boolean ventas = admin || vendedor;
        Long responsable = vendedor ? cuenta.usuario() : null;
        var dias = ventas ? datos.dias(periodo.desde(), periodo.hasta(), responsable, true, admin) : List.<Dia>of();
        var porCombustible = ventas ? barras(datos.litrosVendidos(periodo.desde(), periodo.hasta(), responsable)) : List.<Barra>of();
        BigDecimal saldoActual = admin
            ? datos.dias(periodo.hoy(), periodo.hoy(), null, false, true).getFirst().saldoActual() : null;
        var compras = admin ? datos.comprasPorProveedor(periodo.desde(), periodo.hasta()) : List.<ProveedorCompra>of();
        long movimientos = admin || "OPERADOR".equals(cuenta.rol())
            ? datos.movimientosInventario(periodo.desde(), periodo.hasta()) : 0L;
        return new Resumen(periodo, ventas, admin, dias, inventario(), sumar(dias, Dia::ventas),
            sumar(dias, Dia::litros), dias.stream().mapToLong(Dia::cantidadVentas).sum(),
            porCombustible, ventas ? Grafico.linea(dias, "Ventas", Dia::ventas) : null,
            sumar(dias, Dia::ingresos), sumar(dias, Dia::egresos), saldoActual,
            dias.stream().anyMatch(Dia::tieneCaja), admin ? Grafico.caja(dias) : null,
            admin ? Grafico.linea(dias, "Saldo de cierre", Dia::saldoCierre) : null,
            admin ? comparar(periodo) : null, compras, movimientos);
    }

    private Comparacion comparar(Periodo periodo) {
        long longitud = ChronoUnit.DAYS.between(periodo.desde(), periodo.hasta()) + 1;
        var antHasta = periodo.desde().minusDays(1);
        var antDesde = antHasta.minusDays(longitud - 1);
        var anterior = new Periodo(antDesde, antHasta, periodo.hoy());
        var dias = datos.dias(antDesde, antHasta, null, true, true);
        return new Comparacion(anterior, sumar(dias, Dia::ventas), sumar(dias, Dia::ingresos),
            sumar(dias, Dia::egresos), dias.stream().mapToLong(Dia::cantidadVentas).sum());
    }

    @Override
    public Inventario inventario() {
        var productos = datos.existencias();
        BigDecimal soloLitros = productos.stream()
            .filter(p -> "L".equals(p.unidad()))
            .map(CombustiblePanel::cantidad)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        boolean mixtas = productos.stream().map(CombustiblePanel::unidad).distinct().count() > 1;
        return new Inventario(soloLitros, barras(productos), mixtas);
    }

    /** Porcentaje relativo solo dentro de la misma unidad (L, gal, m3). */
    private List<Barra> barras(List<CombustiblePanel> combustibles) {
        Map<String, BigDecimal> maximoPorUnidad = combustibles.stream().collect(Collectors.toMap(
            p -> p.unidad() == null ? "L" : p.unidad(),
            CombustiblePanel::cantidad,
            BigDecimal::max));
        return combustibles.stream().map(p -> {
            String unidad = p.unidad() == null ? "L" : p.unidad();
            BigDecimal mayor = maximoPorUnidad.getOrDefault(unidad, BigDecimal.ONE);
            if (mayor.signum() == 0) {
                mayor = BigDecimal.ONE;
            }
            return new Barra(p, p.cantidad().multiply(BigDecimal.valueOf(100))
                .divide(mayor, 0, RoundingMode.HALF_UP).intValueExact());
        }).toList();
    }

    private BigDecimal sumar(List<Dia> dias, Function<Dia, BigDecimal> campo) {
        return dias.stream().map(campo).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
