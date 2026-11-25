package com.estacionnexo.asistencia.model;

import java.time.LocalDate;
import java.time.LocalTime;

public record Jornada(long id, long empleado, String nombre, LocalDate fecha, LocalTime entrada,
                      LocalTime salida, String estado) {}
