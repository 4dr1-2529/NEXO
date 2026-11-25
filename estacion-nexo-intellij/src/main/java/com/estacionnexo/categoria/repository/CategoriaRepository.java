package com.estacionnexo.categoria.repository;

import com.estacionnexo.categoria.dto.CategoriaDatos;
import com.estacionnexo.categoria.model.Categoria;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface CategoriaRepository {
    List<Categoria> listar(boolean incluirInactivos, int limite, long inicio, Long categoria);
    Optional<Categoria> buscar(long id, boolean incluirInactivos);
    Optional<Categoria> buscarPorNombre(String nombre, boolean incluirInactivos);
    long crear(CategoriaDatos datos, LocalDateTime fecha);
    boolean editar(long id, CategoriaDatos datos);
    boolean cambiarEstado(long id, boolean activo);
}
