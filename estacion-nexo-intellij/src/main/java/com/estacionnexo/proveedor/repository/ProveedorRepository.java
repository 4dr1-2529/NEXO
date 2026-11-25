package com.estacionnexo.proveedor.repository;

import com.estacionnexo.proveedor.dto.ProveedorDatos;
import com.estacionnexo.proveedor.model.Proveedor;
import java.util.List;
import java.util.Optional;

public interface ProveedorRepository {
    List<Proveedor> listar(boolean incluirInactivos, int limite, long inicio, Long categoria);
    Optional<Proveedor> buscar(long id, boolean incluirInactivos);
    long crear(ProveedorDatos datos);
    boolean editar(long id, ProveedorDatos datos);
    boolean cambiarEstado(long id, boolean activo);
}
