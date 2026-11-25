package com.estacionnexo.producto.service.impl;

import com.estacionnexo.auditoria.AuditoriaContexto;
import com.estacionnexo.producto.dto.ProductoDatos;
import com.estacionnexo.producto.model.Producto;
import com.estacionnexo.producto.repository.ProductoRepository;
import com.estacionnexo.producto.service.ProductoService;
import com.estacionnexo.exception.ReglaIncumplida;
import com.estacionnexo.categoria.service.CategoriaService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductoServiceImpl implements ProductoService {
    private final ProductoRepository registros;
    private final CategoriaService categorias;
    private final AuditoriaContexto auditoria;

    public ProductoServiceImpl(ProductoRepository registros, CategoriaService categorias,
                               AuditoriaContexto auditoria) {
        this.registros = registros;
        this.categorias = categorias;
        this.auditoria = auditoria;
    }

    @Override
    public List<Producto> listar(boolean inactivos, int pagina, Long categoria) {
        if (pagina < 0) {
            throw new ReglaIncumplida("La página debe ser igual o mayor que cero.");
        }
        return registros.listar(inactivos, 25, pagina * 25L, categoria);
    }

    @Override
    public Producto buscar(long id, boolean inactivos) {
        return registros.buscar(id, inactivos)
            .orElseThrow(() -> new ReglaIncumplida("El registro solicitado no está disponible."));
    }

    @Override
    @Transactional
    public long crear(ProductoDatos datos) {
        categorias.buscar(datos.categoria(), false);
        long id = registros.crear(datos);
        auditoria.ok("CREAR", "PRODUCTOS", datos.codigo(), null,
            datos.nombre() + ";" + datos.unidadMedida());
        return id;
    }

    @Override
    @Transactional
    public void editar(long id, ProductoDatos datos) {
        var anterior = buscar(id, true);
        categorias.buscar(datos.categoria(), false);
        registros.editar(id, datos);
        auditoria.ok("EDITAR", "PRODUCTOS", datos.codigo(),
            anterior.datos().nombre() + ";" + anterior.datos().precio(),
            datos.nombre() + ";" + datos.precio() + ";" + datos.unidadMedida());
    }

    @Override
    @Transactional
    public void cambiarEstado(long id, boolean activo) {
        var registro = buscar(id, true);
        if (activo) {
            categorias.buscar(registro.datos().categoria(), false);
        }
        registros.cambiarEstado(id, activo);
        auditoria.ok(activo ? "ACTIVAR" : "DESACTIVAR", "PRODUCTOS", registro.datos().codigo(),
            registro.activo() ? "Activo" : "Inactivo", activo ? "Activo" : "Inactivo");
    }
}
