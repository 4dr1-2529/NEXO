package com.estacionnexo.proveedor.service.impl;

import com.estacionnexo.proveedor.dto.ProveedorDatos;
import com.estacionnexo.proveedor.model.Proveedor;
import com.estacionnexo.proveedor.repository.ProveedorRepository;
import com.estacionnexo.proveedor.service.ProveedorService;
import com.estacionnexo.exception.ReglaIncumplida;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProveedorServiceImpl implements ProveedorService {
    private final ProveedorRepository registros;

    public ProveedorServiceImpl(ProveedorRepository registros) {
        this.registros = registros;
    }

    @Override
    public List<Proveedor> listar(boolean inactivos, int pagina, Long categoria) {
        if (pagina < 0) {
            throw new ReglaIncumplida("La página debe ser igual o mayor que cero.");
        }
        return registros.listar(inactivos, 25, pagina * 25L, categoria);
    }

    @Override
    public Proveedor buscar(long id, boolean inactivos) {
        return registros.buscar(id, inactivos)
            .orElseThrow(() -> new ReglaIncumplida("El registro solicitado no está disponible."));
    }

    @Override
    @Transactional
    public long crear(ProveedorDatos datos) {
        return registros.crear(datos);
    }

    @Override
    @Transactional
    public void editar(long id, ProveedorDatos datos) {
        buscar(id, true);
        registros.editar(id, datos);
    }

    @Override
    @Transactional
    public void cambiarEstado(long id, boolean activo) {
        var registro = buscar(id, true);
        registros.cambiarEstado(id, activo);
    }
}
