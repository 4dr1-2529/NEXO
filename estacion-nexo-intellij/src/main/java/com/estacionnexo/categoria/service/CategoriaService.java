package com.estacionnexo.categoria.service;

import com.estacionnexo.categoria.dto.CategoriaDatos;
import com.estacionnexo.categoria.model.Categoria;
import java.util.List;

public interface CategoriaService {
    List<Categoria> listar(boolean inactivos, int pagina, Long categoria);
    Categoria buscar(long id, boolean inactivos);
    long crear(CategoriaDatos datos);
    void editar(long id, CategoriaDatos datos);
    void cambiarEstado(long id, boolean activo);
}
