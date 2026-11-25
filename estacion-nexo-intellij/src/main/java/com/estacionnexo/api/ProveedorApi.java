package com.estacionnexo.api;

import com.estacionnexo.proveedor.dto.ProveedorDatos;
import com.estacionnexo.proveedor.model.Proveedor;
import com.estacionnexo.proveedor.service.ProveedorService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/proveedores")
public class ProveedorApi {
    private final ProveedorService servicio;

    public ProveedorApi(ProveedorService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    List<Proveedor> listar(Authentication autenticacion, @RequestParam(defaultValue = "0") int pagina) {
        return servicio.listar(admin(autenticacion), pagina, null);
    }

    @GetMapping("/{id}")
    Proveedor buscar(Authentication autenticacion, @PathVariable long id) {
        return servicio.buscar(id, admin(autenticacion));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    OperacionesApi.Id crear(@Valid @RequestBody ProveedorDatos datos) {
        return new OperacionesApi.Id(servicio.crear(datos));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void editar(@PathVariable long id, @Valid @RequestBody ProveedorDatos datos) {
        servicio.editar(id, datos);
    }

    @PatchMapping("/{id}/estado")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void activar(@PathVariable long id, @RequestParam boolean activo) {
        servicio.cambiarEstado(id, activo);
    }

    private boolean admin(Authentication autenticacion) {
        return autenticacion.getAuthorities().stream()
            .anyMatch(autoridad -> "ROLE_ADMINISTRADOR".equals(autoridad.getAuthority()));
    }
}
