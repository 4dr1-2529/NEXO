package com.estacionnexo.api;

import com.estacionnexo.categoria.dto.CategoriaDatos;
import com.estacionnexo.categoria.model.Categoria;
import com.estacionnexo.categoria.service.CategoriaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/categorias")
public class CategoriaApi {
    private final CategoriaService servicio;

    public CategoriaApi(CategoriaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    List<Categoria> listar(Authentication autenticacion, @RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "false") boolean archivados) {
        return servicio.listar(admin(autenticacion) && archivados, pagina, null);
    }

    @GetMapping("/{id}")
    Categoria buscar(Authentication autenticacion, @PathVariable long id) {
        return servicio.buscar(id, admin(autenticacion));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    OperacionesApi.Id crear(@Valid @RequestBody CategoriaDatos datos) {
        return new OperacionesApi.Id(servicio.crear(datos));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void editar(@PathVariable long id, @Valid @RequestBody CategoriaDatos datos) {
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
