package com.estacionnexo.producto.controller;

import com.estacionnexo.producto.dto.ProductoDatos;
import com.estacionnexo.categoria.model.Categoria;
import com.estacionnexo.categoria.service.CategoriaService;
import java.util.ArrayList;
import com.estacionnexo.producto.service.ProductoService;
import com.estacionnexo.exception.ReglaIncumplida;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/combustibles")
public class ProductoController {
    private final ProductoService servicio;

    private final CategoriaService categorias;

    public ProductoController(ProductoService servicio, CategoriaService categorias) {
        this.servicio = servicio;
        this.categorias = categorias;
    }

    @GetMapping
    String listar(Authentication auth, @RequestParam(defaultValue = "0") int pagina,
                  @RequestParam(required = false) Long categoria,
                  @RequestParam(defaultValue = "false") boolean archivados, Model model) {
        boolean admin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));
        int actual = Math.max(0, pagina);
        var registros = servicio.listar(admin && archivados, actual, categoria);
        var nombres = new java.util.HashMap<Long, String>();
        for (var producto : registros) {
            nombres.computeIfAbsent(producto.datos().categoria(),
                id -> categorias.buscar(id, true).datos().nombre());
        }
        model.addAttribute("nombresCategoria", nombres);
        model.addAttribute("registros", registros);
        model.addAttribute("admin", admin);
        model.addAttribute("archivados", admin && archivados);
        model.addAttribute("pagina", actual);
        model.addAttribute("categoria", categoria);
        return "combustibles/lista";
    }

    @GetMapping("/nuevo")
    String nuevo(Model model) {
        model.addAttribute("datos", new ProductoDatos(null, null, null, BigDecimal.ZERO));
        return formulario(null, model);
    }

    @GetMapping("/{id}/editar")
    String editar(@PathVariable long id, Model model) {
        model.addAttribute("datos", servicio.buscar(id, true).datos());
        return formulario(id, model);
    }

    @PostMapping
    String crear(@Valid @ModelAttribute("datos") ProductoDatos datos, BindingResult resultado,
                 Model model, RedirectAttributes redireccion) {
        return guardar(null, datos, resultado, model, redireccion);
    }

    @PostMapping("/{id}/editar")
    String editar(@PathVariable long id, @Valid @ModelAttribute("datos") ProductoDatos datos,
                  BindingResult resultado, Model model, RedirectAttributes redireccion) {
        return guardar(id, datos, resultado, model, redireccion);
    }

    @PostMapping("/{id}/estado")
    String estado(@PathVariable long id, @RequestParam boolean activo, RedirectAttributes redireccion) {
        try {
            servicio.cambiarEstado(id, activo);
            redireccion.addFlashAttribute("mensaje", "Estado actualizado.");
        } catch (ReglaIncumplida e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/combustibles";
    }

    private String guardar(Long id, ProductoDatos datos, BindingResult resultado, Model model,
                           RedirectAttributes redireccion) {
        if (resultado.hasErrors()) {
            return formulario(id, model);
        }
        try {
            if (id == null) {
                servicio.crear(datos);
            } else {
                servicio.editar(id, datos);
            }
        } catch (DuplicateKeyException e) {
            model.addAttribute("error", "Ya existe un registro con esos datos.");
            return formulario(id, model);
        } catch (ReglaIncumplida e) {
            model.addAttribute("error", e.getMessage());
            return formulario(id, model);
        }
        redireccion.addFlashAttribute("mensaje", "Registro guardado.");
        return "redirect:/combustibles";
    }

    private String formulario(Long id, Model model) {
        model.addAttribute("id", id);
        var opciones = new ArrayList<Categoria>();
        for (int pagina = 0; ; pagina++) {
            var lote = categorias.listar(false, pagina, null);
            opciones.addAll(lote);
            if (lote.size() < 25) {
                break;
            }
        }
        model.addAttribute("opcionesCategoria", opciones);
        return "combustibles/formulario";
    }
}
