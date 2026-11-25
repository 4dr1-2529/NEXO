package com.estacionnexo.shared.consulta.service;

import com.estacionnexo.auth.model.Cuenta;
import com.estacionnexo.shared.consulta.model.Detalle;
import com.estacionnexo.shared.consulta.model.Registro;
import com.estacionnexo.shared.consulta.repository.ConsultaRepository.Tipo;
import java.time.LocalDate;
import java.util.List;

public interface ConsultaService {
    List<Registro> listar(Tipo tipo, Cuenta cuenta, LocalDate desde, LocalDate hasta, String filtro, int pagina);
    Resultado detalle(Tipo tipo, Cuenta cuenta, long id);
    record Resultado(Registro registro, List<Detalle> lineas) {}
}
