package com.estacionnexo.proveedor.service;

import com.estacionnexo.proveedor.dto.ProveedorDatos;
import com.estacionnexo.proveedor.model.Proveedor;
import java.util.List;

public interface ProveedorService {
    List<Proveedor> listar(boolean inactivos, int pagina, Long categoria);
    Proveedor buscar(long id, boolean inactivos);
    long crear(ProveedorDatos datos);
    void editar(long id, ProveedorDatos datos);
    void cambiarEstado(long id, boolean activo);
}
