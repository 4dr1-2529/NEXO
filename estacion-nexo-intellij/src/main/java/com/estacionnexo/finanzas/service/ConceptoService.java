package com.estacionnexo.finanzas.service;

import com.estacionnexo.finanzas.dto.ConceptoDatos;
import com.estacionnexo.finanzas.model.Concepto;
import java.util.List;

public interface ConceptoService {
    List<Concepto> listar(int pagina);
    Concepto buscar(long id);
    void guardar(Long id, ConceptoDatos datos);
    void cambiarEstado(long id, boolean activo);
}
