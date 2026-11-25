package com.estacionnexo.finanzas.model;

import com.estacionnexo.finanzas.dto.ConceptoDatos;

public record Concepto(long id, ConceptoDatos datos, boolean activo) {}
