package com.estacionnexo.finanzas.service.impl;

import com.estacionnexo.exception.ReglaIncumplida;
import com.estacionnexo.finanzas.dto.ConceptoDatos;
import com.estacionnexo.finanzas.model.Concepto;
import com.estacionnexo.finanzas.repository.ConceptoRepository;
import com.estacionnexo.finanzas.service.ConceptoService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConceptoServiceImpl implements ConceptoService {
    private final ConceptoRepository conceptos;

    public ConceptoServiceImpl(ConceptoRepository conceptos) {
        this.conceptos = conceptos;
    }

    @Override
    public List<Concepto> listar(int pagina) {
        return conceptos.listar(25, Math.max(0, pagina) * 25L);
    }

    @Override
    public Concepto buscar(long id) {
        return conceptos.buscar(id).orElseThrow(() -> new ReglaIncumplida("Concepto no encontrado."));
    }

    @Override
    @Transactional
    public void guardar(Long id, ConceptoDatos datos) {
        if (id == null) {
            conceptos.crear(datos);
            return;
        }
        var anterior = buscar(id).datos();
        if (!anterior.codigo().equals(datos.codigo()) || anterior.tipo() != datos.tipo()) {
            throw new ReglaIncumplida("El código y el tipo se conservan para las operaciones históricas.");
        }
        conceptos.editar(id, datos.nombre().strip());
    }

    @Override
    @Transactional
    public void cambiarEstado(long id, boolean activo) {
        buscar(id);
        conceptos.cambiarEstado(id, activo);
    }
}
