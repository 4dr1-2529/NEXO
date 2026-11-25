package com.estacionnexo.shared.consulta.repository;

import com.estacionnexo.shared.consulta.model.Detalle;
import com.estacionnexo.shared.consulta.model.Registro;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ConsultaRepository {
    enum Tipo { COMPRAS, VENTAS, INVENTARIO, CAJA }
    List<Registro> listar(Tipo tipo, LocalDateTime desde, LocalDateTime hasta, Long usuario,
                          String filtro, int limite, long inicio);
    Optional<Registro> buscar(Tipo tipo, long id, Long usuario);
    List<Detalle> detalles(Tipo tipo, long id);
}
