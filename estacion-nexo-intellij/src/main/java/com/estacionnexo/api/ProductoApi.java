package com.estacionnexo.api;

import com.estacionnexo.producto.dto.ProductoDatos;
import com.estacionnexo.producto.model.Producto;
import com.estacionnexo.producto.service.ProductoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/combustibles")
public class ProductoApi {
    private final ProductoService servicio;

    public ProductoApi(ProductoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    List<Producto> listar(Authentication autenticacion, @RequestParam(defaultValue = "0") int pagina, @RequestParam(required = false) Long categoria, @RequestParam(defaultValue = "false") boolean archivados) {
        return servicio.listar(admin(autenticacion) && archivados, pagina, categoria);
    }

    @GetMapping("/{id}")
    Producto buscar(Authentication autenticacion, @PathVariable long id) {
        return servicio.buscar(id, admin(autenticacion));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    OperacionesApi.Id crear(@Valid @RequestBody ProductoDatos datos) {
        return new OperacionesApi.Id(servicio.crear(datos));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void editar(@PathVariable long id, @Valid @RequestBody ProductoDatos datos) {
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
