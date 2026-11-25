package com.estacionnexo.empleado.service.impl;

import com.estacionnexo.empleado.dto.EmpleadoDatos;
import com.estacionnexo.empleado.model.Empleado;
import com.estacionnexo.empleado.repository.EmpleadoRepository;
import com.estacionnexo.empleado.service.EmpleadoService;
import com.estacionnexo.exception.ReglaIncumplida;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmpleadoServiceImpl implements EmpleadoService {
    private final EmpleadoRepository empleados;

    public EmpleadoServiceImpl(EmpleadoRepository empleados) {
        this.empleados = empleados;
    }

    @Override
    public List<Empleado> listar(int pagina) {
        return empleados.listar(Math.max(0, pagina) * 25L, 25);
    }

    @Override
    public Empleado buscar(long id) {
        return empleados.buscar(id).orElseThrow(() -> new ReglaIncumplida("Empleado no encontrado."));
    }

    @Override
    public long crear(EmpleadoDatos datos) {
        return empleados.insertar(datos);
    }

    @Override
    @Transactional
    public void editar(long id, EmpleadoDatos datos) {
        buscar(id);
        empleados.editar(id, datos);
    }

    @Override
    @Transactional
    public void cambiarEstado(long id, boolean activo, long empleadoActual) {
        if (!activo && id == empleadoActual) {
            throw new ReglaIncumplida("No puedes desactivar tu propio registro de empleado.");
        }
        buscar(id);
        empleados.estado(id, activo);
    }
}
