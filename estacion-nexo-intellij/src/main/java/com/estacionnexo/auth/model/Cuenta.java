package com.estacionnexo.auth.model;

public record Cuenta(long usuario, long empleado, String username, String hash, String rol,
                     String nombre, boolean activa) {}
