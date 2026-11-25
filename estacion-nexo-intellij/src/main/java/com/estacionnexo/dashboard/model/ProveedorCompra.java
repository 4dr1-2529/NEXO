package com.estacionnexo.dashboard.model;

import java.math.BigDecimal;

public record ProveedorCompra(String ruc, String razonSocial, long compras, BigDecimal total) {}
