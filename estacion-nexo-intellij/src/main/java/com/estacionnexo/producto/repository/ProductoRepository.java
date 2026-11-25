package com.estacionnexo.producto.repository;

import com.estacionnexo.producto.dto.ProductoDatos;
import com.estacionnexo.producto.model.Producto;
import java.util.List;
import java.util.Optional;

public interface ProductoRepository {
    List<Producto> listar(boolean incluirInactivos, int limite, long inicio, Long categoria);
    Optional<Producto> buscar(long id, boolean incluirInactivos);
    Optional<Producto> buscarPorCodigo(String codigo, boolean incluirInactivos);
    long crear(ProductoDatos datos);
    boolean editar(long id, ProductoDatos datos);
    boolean sincronizarMetadatos(String codigo, long categoria, String nombre, String unidad);
    boolean cambiarEstado(long id, boolean activo);
}
