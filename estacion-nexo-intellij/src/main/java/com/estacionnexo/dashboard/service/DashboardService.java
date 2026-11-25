package com.estacionnexo.dashboard.service;

import com.estacionnexo.auth.model.Cuenta;
import com.estacionnexo.dashboard.model.AnalisisVentas;
import com.estacionnexo.dashboard.model.CombustiblePanel;
import com.estacionnexo.dashboard.model.Composicion;
import com.estacionnexo.dashboard.model.Dia;
import com.estacionnexo.dashboard.model.Grafico;
import com.estacionnexo.dashboard.model.ProveedorCompra;
import com.estacionnexo.shared.Periodo;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface DashboardService {
    Resumen cargar(Cuenta cuenta, LocalDate desde, LocalDate hasta);
    Inventario inventario();

    record Resumen(Periodo periodo, boolean ventas, boolean finanzas, List<Dia> dias, Inventario inventario,
                   BigDecimal totalVentas, BigDecimal totalLitros, long cantidadVentas,
                   List<Barra> combustiblesVendidos, Grafico lineaVentas,
                   BigDecimal totalIngresos, BigDecimal totalEgresos, BigDecimal saldoActual,
                   boolean hayCajas, Grafico flujosCaja, Grafico cierresCaja,
                   Comparacion comparacion, List<ProveedorCompra> comprasProveedor,
                   long movimientosInventario) {
        public AnalisisVentas analisisVentas() {
            return AnalisisVentas.de(dias);
        }

        public Composicion composicion() {
            return Composicion.de(combustiblesVendidos.stream().map(Barra::dato).toList());
        }

        public List<Actividad> actividadDiaria() {
            long maximo = dias.stream().mapToLong(Dia::cantidadVentas).max().orElse(0);
            return dias.stream().map(dia -> new Actividad(dia.fecha(), dia.cantidadVentas(),
                dia.cantidadVentas() == 0 ? 0 : (int) Math.ceil(4.0 * dia.cantidadVentas() / maximo))).toList();
        }
    }

    record Comparacion(Periodo anterior, BigDecimal ventasAnterior, BigDecimal ingresosAnterior,
                       BigDecimal egresosAnterior, long ventasCantidadAnterior) {}

    record Actividad(LocalDate fecha, long ventas, int nivel) {
        public String etiqueta() {
            return fecha.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM"));
        }
    }

    /** {@code litros} solo suma existencias con unidad L; no mezcla gal/m3. */
    record Inventario(BigDecimal litros, List<Barra> productos, boolean unidadesMixtas) {
        public Inventario(BigDecimal litros, List<Barra> productos) {
            this(litros, productos, productos.stream().map(b -> b.dato().unidad()).distinct().count() > 1);
        }

        public long bajoMinimo() {
            return productos.stream().filter(p -> p.dato().bajo()).count();
        }

        public long agotados() {
            return productos.stream().filter(p -> p.dato().cantidad().signum() == 0).count();
        }
    }

    record Barra(CombustiblePanel dato, int avance) {}
}
