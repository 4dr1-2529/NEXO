package com.estacionnexo.categoria.service.impl;

import com.estacionnexo.auditoria.AuditoriaContexto;
import com.estacionnexo.categoria.dto.CategoriaDatos;
import com.estacionnexo.categoria.model.Categoria;
import com.estacionnexo.categoria.repository.CategoriaRepository;
import com.estacionnexo.categoria.service.CategoriaService;
import com.estacionnexo.exception.ReglaIncumplida;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoriaServiceImpl implements CategoriaService {
    private final CategoriaRepository registros;
    private final Clock reloj;
    private final AuditoriaContexto auditoria;

    public CategoriaServiceImpl(CategoriaRepository registros, Clock reloj, AuditoriaContexto auditoria) {
        this.registros = registros;
        this.reloj = reloj;
        this.auditoria = auditoria;
    }

    @Override
    public List<Categoria> listar(boolean inactivos, int pagina, Long categoria) {
        if (pagina < 0) {
            throw new ReglaIncumplida("La página debe ser igual o mayor que cero.");
        }
        return registros.listar(inactivos, 25, pagina * 25L, categoria);
    }

    @Override
    public Categoria buscar(long id, boolean inactivos) {
        return registros.buscar(id, inactivos)
            .orElseThrow(() -> new ReglaIncumplida("El registro solicitado no está disponible."));
    }

    @Override
    @Transactional
    public long crear(CategoriaDatos datos) {
        long id = registros.crear(datos, LocalDateTime.now(reloj));
        auditoria.ok("CREAR", "CATEGORIAS", datos.nombre(), null, datos.descripcion());
        return id;
    }

    @Override
    @Transactional
    public void editar(long id, CategoriaDatos datos) {
        var anterior = buscar(id, true);
        registros.editar(id, datos);
        auditoria.ok("EDITAR", "CATEGORIAS", datos.nombre(), anterior.datos().nombre(), datos.descripcion());
    }

    @Override
    @Transactional
    public void cambiarEstado(long id, boolean activo) {
        var registro = buscar(id, true);
        registros.cambiarEstado(id, activo);
        auditoria.ok(activo ? "ACTIVAR" : "DESACTIVAR", "CATEGORIAS", registro.datos().nombre(),
            registro.activo() ? "Activo" : "Inactivo", activo ? "Activo" : "Inactivo");
    }
}
