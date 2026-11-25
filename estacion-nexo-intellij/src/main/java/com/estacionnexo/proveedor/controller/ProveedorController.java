package com.estacionnexo.proveedor.controller;

import com.estacionnexo.proveedor.dto.ProveedorDatos;
import com.estacionnexo.proveedor.service.ProveedorService;
import com.estacionnexo.exception.ReglaIncumplida;
import jakarta.validation.Valid;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/proveedores")
public class ProveedorController {
    private final ProveedorService servicio;

    public ProveedorController(ProveedorService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    String listar(Authentication auth, @RequestParam(defaultValue = "0") int pagina,
                  @RequestParam(required = false) Long categoria, Model model) {
        boolean admin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));
        int actual = Math.max(0, pagina);
        var registros = servicio.listar(admin, actual, categoria);
        model.addAttribute("registros", registros);
        model.addAttribute("admin", admin);
        model.addAttribute("pagina", actual);
        model.addAttribute("categoria", categoria);
        return "proveedores/lista";
    }

    @GetMapping("/nuevo")
    String nuevo(Model model) {
        model.addAttribute("datos", new ProveedorDatos(null, null, null, null, null, null));
        return formulario(null, model);
    }

    @GetMapping("/{id}/editar")
    String editar(@PathVariable long id, Model model) {
        model.addAttribute("datos", servicio.buscar(id, true).datos());
        return formulario(id, model);
    }

    @PostMapping
    String crear(@Valid @ModelAttribute("datos") ProveedorDatos datos, BindingResult resultado,
                 Model model, RedirectAttributes redireccion) {
        return guardar(null, datos, resultado, model, redireccion);
    }

    @PostMapping("/{id}/editar")
    String editar(@PathVariable long id, @Valid @ModelAttribute("datos") ProveedorDatos datos,
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
        return "redirect:/proveedores";
    }

    private String guardar(Long id, ProveedorDatos datos, BindingResult resultado, Model model,
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
        return "redirect:/proveedores";
    }

    private String formulario(Long id, Model model) {
        model.addAttribute("id", id);
        return "proveedores/formulario";
    }
}
