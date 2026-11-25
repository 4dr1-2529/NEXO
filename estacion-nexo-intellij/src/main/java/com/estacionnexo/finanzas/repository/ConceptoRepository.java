package com.estacionnexo.finanzas.repository;

import com.estacionnexo.finanzas.dto.ConceptoDatos;
import com.estacionnexo.finanzas.model.Concepto;
import java.util.List;
import java.util.Optional;

public interface ConceptoRepository {
    List<Concepto> listar(int limite, long inicio);
    Optional<Concepto> buscar(long id);
    void crear(ConceptoDatos datos);
    void editar(long id, String nombre);
    void cambiarEstado(long id, boolean activo);
}
