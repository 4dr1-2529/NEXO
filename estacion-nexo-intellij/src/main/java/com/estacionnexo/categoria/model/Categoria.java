package com.estacionnexo.categoria.model;

import com.estacionnexo.categoria.dto.CategoriaDatos;

public record Categoria(long id, CategoriaDatos datos, boolean activo) {}
