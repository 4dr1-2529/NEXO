package com.estacionnexo.config;

import com.estacionnexo.categoria.dto.CategoriaDatos;
import com.estacionnexo.categoria.repository.CategoriaRepository;
import com.estacionnexo.producto.CatalogoCombustibles;
import com.estacionnexo.producto.dto.ProductoDatos;
import com.estacionnexo.producto.repository.ProductoRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Asegura categorías y metadatos del catálogo canónico en cualquier perfil.
 * No sobrescribe precios ni existencias. Solo crea productos faltantes si hay
 * precio configurado en {@code nexo.catalogo.precio.<CODIGO>} (p. ej. perfil demostracion).
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class CatalogoCanonico implements ApplicationRunner {
    private static final Logger LOG = LoggerFactory.getLogger(CatalogoCanonico.class);
    private static final Map<String, String> DESCRIPCIONES = Map.of(
        CatalogoCombustibles.CAT_GASOLINAS, CatalogoCombustibles.DESC_GASOLINAS,
        CatalogoCombustibles.CAT_DIESEL, CatalogoCombustibles.DESC_DIESEL,
        CatalogoCombustibles.CAT_GASES, CatalogoCombustibles.DESC_GASES);

    private final CategoriaRepository categorias;
    private final ProductoRepository productos;
    private final Environment entorno;
    private final Clock reloj;

    public CatalogoCanonico(CategoriaRepository categorias, ProductoRepository productos,
                            Environment entorno, Clock reloj) {
        this.categorias = categorias;
        this.productos = productos;
        this.entorno = entorno;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        asegurarCategorias();
        for (var item : CatalogoCombustibles.ACTIVOS) {
            asegurarProducto(item);
        }
    }

    private void asegurarCategorias() {
        for (var entrada : DESCRIPCIONES.entrySet()) {
            categorias.buscarPorNombre(entrada.getKey(), true).ifPresentOrElse(
                existente -> {
                    if (!entrada.getValue().equals(existente.datos().descripcion())) {
                        categorias.editar(existente.id(),
                            new CategoriaDatos(entrada.getKey(), entrada.getValue()));
                    }
                },
                () -> categorias.crear(new CategoriaDatos(entrada.getKey(), entrada.getValue()),
                    LocalDateTime.now(reloj)));
        }
    }

    private void asegurarProducto(CatalogoCombustibles.Item item) {
        long categoriaId = categorias.buscarPorNombre(item.categoria(), true)
            .orElseThrow(() -> new IllegalStateException("Falta categoría " + item.categoria()))
            .id();
        var existente = productos.buscarPorCodigo(item.codigo(), true);
        if (existente.isPresent()) {
            productos.sincronizarMetadatos(item.codigo(), categoriaId, item.nombre(), item.unidad());
            return;
        }
        var precio = entorno.getProperty("nexo.catalogo.precio." + item.codigo());
        if (precio == null || precio.isBlank()) {
            LOG.info("SKU {} ausente: no se crea sin nexo.catalogo.precio.{} (evita precios ficticios).",
                item.codigo(), item.codigo());
            return;
        }
        productos.crear(new ProductoDatos(item.codigo(), categoriaId, item.nombre(),
            new BigDecimal(precio.strip()), item.unidad()));
        LOG.info("SKU {} incorporado al catálogo con unidad {}.", item.codigo(), item.unidad());
    }
}
