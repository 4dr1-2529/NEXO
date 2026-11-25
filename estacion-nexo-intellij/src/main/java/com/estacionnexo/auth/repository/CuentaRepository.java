package com.estacionnexo.auth.repository;

import com.estacionnexo.auth.model.Cuenta;
import java.time.LocalDateTime;
import java.util.Optional;

public interface CuentaRepository {
    Optional<Cuenta> buscar(String username);
    void registrarAcceso(long usuario, LocalDateTime fecha);
    void crearPrimerAdministrador(String username, String hash, String dni, String nombres,
                                  String apellidos, String telefono, LocalDateTime fecha);
}
