package com.estacionnexo.auth.service;

import com.estacionnexo.auth.model.Cuenta;
import java.util.Optional;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface CuentaService extends UserDetailsService {
    Optional<Cuenta> buscar(String username);
    void registrarAcceso(String username);
    void inicializar(String username, String password, String dni, String nombres, String apellidos, String telefono);
}
