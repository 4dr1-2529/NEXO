package com.estacionnexo.dashboard.repository;

import com.estacionnexo.dashboard.model.CombustiblePanel;
import com.estacionnexo.dashboard.model.Dia;
import com.estacionnexo.dashboard.model.ProveedorCompra;
import java.time.LocalDate;
import java.util.List;

public interface DashboardRepository {
    List<CombustiblePanel> existencias();
    List<CombustiblePanel> litrosVendidos(LocalDate desde, LocalDate hasta, Long vendedor);
    List<Dia> dias(LocalDate desde, LocalDate hasta, Long vendedor, boolean ventas, boolean finanzas);
    List<ProveedorCompra> comprasPorProveedor(LocalDate desde, LocalDate hasta);
    long movimientosInventario(LocalDate desde, LocalDate hasta);
}
