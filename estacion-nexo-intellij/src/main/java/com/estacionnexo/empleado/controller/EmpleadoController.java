package com.estacionnexo.empleado.controller;

import com.estacionnexo.auth.model.Cuenta;
import com.estacionnexo.auth.service.CuentaService;
import com.estacionnexo.exception.ReglaIncumplida;
import com.estacionnexo.empleado.dto.EmpleadoDatos;
import com.estacionnexo.empleado.service.EmpleadoService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/empleados")
public class EmpleadoController {
    private final EmpleadoService servicio;
    private final CuentaService cuentas;

    public EmpleadoController(EmpleadoService servicio, CuentaService cuentas) {
        this.servicio = servicio;
        this.cuentas = cuentas;
    }

    @GetMapping
    String listar(@RequestParam(defaultValue = "0") int pagina, Model model) {
        int actual = Math.max(0, pagina);
        model.addAttribute("registros", servicio.listar(actual));
        model.addAttribute("pagina", actual);
        return "empleados/lista";
    }

    @GetMapping("/nuevo")
    String nuevo(Model model) {
        model.addAttribute("datos", new EmpleadoDatos(null, null, null, null, null, null));
        return formulario(null, model);
    }

    @GetMapping("/{id}/editar")
    String editar(@PathVariable long id, Model model) {
        var registro = servicio.buscar(id);
        model.addAttribute("datos", registro.datos());
        return formulario(id, model);
    }

    @PostMapping
    String crear(@Valid @ModelAttribute("datos") EmpleadoDatos datos, BindingResult validacion,
                 Principal principal, Model model, RedirectAttributes redireccion) {
        return guardar(null, datos, validacion, principal, model, redireccion);
    }

    @PostMapping("/{id}/editar")
    String editar(@PathVariable long id, @Valid @ModelAttribute("datos") EmpleadoDatos datos, BindingResult validacion,
                  Principal principal, Model model, RedirectAttributes redireccion) {
        return guardar(id, datos, validacion, principal, model, redireccion);
    }

    @PostMapping("/{id}/estado")
    String estado(@PathVariable long id, @RequestParam boolean activo, Principal principal,
                  RedirectAttributes redireccion) {
        try {
            servicio.cambiarEstado(id, activo, actor(principal).empleado());
            redireccion.addFlashAttribute("mensaje", "Estado actualizado.");
        } catch (ReglaIncumplida e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/empleados";
    }

    private String guardar(Long id, EmpleadoDatos datos, BindingResult validacion, Principal principal,
                           Model model, RedirectAttributes redireccion) {
        if (validacion.hasErrors()) {
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
        return "redirect:/empleados";
    }

    private String formulario(Long id, Model model) {
        model.addAttribute("id", id);
        return "empleados/formulario";
    }

    private Cuenta actor(Principal principal) {
        return cuentas.buscar(principal.getName()).filter(Cuenta::activa)
            .orElseThrow(() -> new AccessDeniedException("La cuenta no está activa."));
    }
}
