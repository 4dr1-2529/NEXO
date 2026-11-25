package com.estacionnexo.finanzas.controller;

import com.estacionnexo.finanzas.dto.ConceptoDatos;
import com.estacionnexo.finanzas.service.ConceptoService;
import com.estacionnexo.exception.ReglaIncumplida;
import jakarta.validation.Valid;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/finanzas/conceptos")
public class ConceptoController {
    private final ConceptoService conceptos;

    public ConceptoController(ConceptoService conceptos) {
        this.conceptos = conceptos;
    }

    @GetMapping
    String listar(@RequestParam(defaultValue = "0") int pagina, Model model) {
        int actual = Math.max(0, pagina);
        model.addAttribute("registros", conceptos.listar(actual));
        model.addAttribute("pagina", actual);
        return "finanzas/conceptos";
    }

    @GetMapping("/nuevo")
    String nuevo(Model model) {
        model.addAttribute("datos", new ConceptoDatos(null, null, null));
        return formulario(null, model);
    }

    @GetMapping("/{id}/editar")
    String editar(@PathVariable long id, Model model) {
        model.addAttribute("datos", conceptos.buscar(id).datos());
        return formulario(id, model);
    }

    @PostMapping({"", "/{id}/editar"})
    String guardar(@PathVariable(required = false) Long id, @Valid @ModelAttribute("datos") ConceptoDatos datos,
                   BindingResult validacion, Model model, RedirectAttributes redireccion) {
        if (validacion.hasErrors()) {
            return formulario(id, model);
        }
        try {
            conceptos.guardar(id, datos);
            redireccion.addFlashAttribute("mensaje", "Concepto guardado.");
            return "redirect:/finanzas/conceptos";
        } catch (DuplicateKeyException e) {
            model.addAttribute("error", "El código ya está registrado.");
        } catch (ReglaIncumplida e) {
            model.addAttribute("error", e.getMessage());
        }
        return formulario(id, model);
    }

    @PostMapping("/{id}/estado")
    String estado(@PathVariable long id, @RequestParam boolean activo, RedirectAttributes redireccion) {
        conceptos.cambiarEstado(id, activo);
        redireccion.addFlashAttribute("mensaje", "Estado actualizado.");
        return "redirect:/finanzas/conceptos";
    }

    private String formulario(Long id, Model model) {
        model.addAttribute("id", id);
        model.addAttribute("tipos", ConceptoDatos.Tipo.values());
        return "finanzas/concepto";
    }
}
