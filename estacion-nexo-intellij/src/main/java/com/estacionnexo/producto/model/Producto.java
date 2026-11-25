package com.estacionnexo.producto.model;

import com.estacionnexo.producto.dto.ProductoDatos;
import java.math.BigDecimal;

public record Producto(long id, ProductoDatos datos, boolean activo, BigDecimal existencias) {}
