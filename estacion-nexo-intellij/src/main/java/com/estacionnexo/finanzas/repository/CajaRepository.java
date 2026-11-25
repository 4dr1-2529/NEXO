package com.estacionnexo.finanzas.repository;

import com.estacionnexo.finanzas.model.Caja;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public interface CajaRepository {
    Optional<Caja> bloquear(LocalDate fecha);
    Optional<Caja> buscar(LocalDate fecha);
    long abrir(LocalDateTime fecha, long usuario, BigDecimal apertura);
    BigDecimal saldo(Caja caja);
    void cerrar(Caja caja, long usuario, LocalDateTime fecha, BigDecimal saldo);
    void registrar(long caja, long usuario, Long compra, Long venta, BigDecimal monto,
                   LocalDateTime fecha, String codigo);
    void registrarManual(long caja, long usuario, long concepto, String tipo, BigDecimal monto,
                         String descripcion, LocalDateTime fecha, String codigo, String solicitud);
}
