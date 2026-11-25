package com.estacionnexo.proveedor.model;

import com.estacionnexo.proveedor.dto.ProveedorDatos;

public record Proveedor(long id, ProveedorDatos datos, boolean activo) {}
