package com.estacionnexo.usuario.model;

import java.time.LocalDateTime;

public record Usuario(long id, long empleado, long rol, String username, String nombre,
                      String perfil, boolean activo, LocalDateTime ultimoAcceso) {}
