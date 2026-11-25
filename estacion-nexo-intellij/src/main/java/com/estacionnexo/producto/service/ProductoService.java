package com.estacionnexo.producto.service;

import com.estacionnexo.producto.dto.ProductoDatos;
import com.estacionnexo.producto.model.Producto;
import java.util.List;

public interface ProductoService {
    List<Producto> listar(boolean inactivos, int pagina, Long categoria);
    Producto buscar(long id, boolean inactivos);
    long crear(ProductoDatos datos);
    void editar(long id, ProductoDatos datos);
    void cambiarEstado(long id, boolean activo);
}
