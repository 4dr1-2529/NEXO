package com.estacionnexo.empleado.model;

import com.estacionnexo.empleado.dto.EmpleadoDatos;

public record Empleado(long id, EmpleadoDatos datos, boolean activo) {}
