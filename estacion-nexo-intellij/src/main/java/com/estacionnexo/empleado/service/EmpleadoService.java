package com.estacionnexo.empleado.service;

import com.estacionnexo.empleado.dto.EmpleadoDatos;
import com.estacionnexo.empleado.model.Empleado;
import java.util.List;

public interface EmpleadoService {
    List<Empleado> listar(int pagina);
    Empleado buscar(long id);
    long crear(EmpleadoDatos datos);
    void editar(long id, EmpleadoDatos datos);
    void cambiarEstado(long id, boolean activo, long empleadoActual);
}
