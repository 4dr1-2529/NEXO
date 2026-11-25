package com.estacionnexo.empleado.repository;

import com.estacionnexo.empleado.dto.EmpleadoDatos;
import com.estacionnexo.empleado.model.Empleado;
import java.util.List;
import java.util.Optional;

public interface EmpleadoRepository {
    List<Empleado> listar(long inicio, int limite);
    Optional<Empleado> buscar(long id);
    long insertar(EmpleadoDatos datos);
    void editar(long id, EmpleadoDatos datos);
    void estado(long id, boolean activo);
}
