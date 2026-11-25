package com.estacionnexo.auth.service.impl;

import com.estacionnexo.auth.model.Cuenta;
import com.estacionnexo.auth.repository.CuentaRepository;
import com.estacionnexo.auth.service.CuentaService;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CuentaServiceImpl implements CuentaService {
    private final CuentaRepository cuentas;
    private final PasswordEncoder encoder;
    private final Clock reloj;

    public CuentaServiceImpl(CuentaRepository cuentas, PasswordEncoder encoder, Clock reloj) {
        this.cuentas = cuentas;
        this.encoder = encoder;
        this.reloj = reloj;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        var cuenta = buscar(username).orElseThrow(() -> new UsernameNotFoundException("Cuenta desconocida"));
        return User.withUsername(cuenta.username()).password(cuenta.hash()).roles(cuenta.rol())
            .disabled(!cuenta.activa()).build();
    }

    @Override
    public Optional<Cuenta> buscar(String username) {
        return cuentas.buscar(username);
    }

    @Override
    public void registrarAcceso(String username) {
        buscar(username).ifPresent(cuenta -> cuentas.registrarAcceso(cuenta.usuario(), LocalDateTime.now(reloj)));
    }

    @Override
    @Transactional
    public void inicializar(String username, String password, String dni, String nombres,
                            String apellidos, String telefono) {
        if (!username.matches("[A-Za-z0-9._-]{3,30}") || password.codePointCount(0, password.length()) < 12
                || password.getBytes(StandardCharsets.UTF_8).length > 72 || !dni.matches("[0-9]{8}")
                || nombres.isBlank() || nombres.length() > 60 || apellidos.isBlank() || apellidos.length() > 60
                || !telefono.matches("[0-9]{9}")) {
            throw new IllegalArgumentException("Revise los datos configurados para el primer administrador.");
        }
        cuentas.crearPrimerAdministrador(username, encoder.encode(password), dni, nombres.strip(),
            apellidos.strip(), telefono.strip(), LocalDateTime.now(reloj));
    }
}
