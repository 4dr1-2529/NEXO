package com.estacionnexo.asistencia.controller;

import com.estacionnexo.asistencia.service.AsistenciaService;
import com.estacionnexo.exception.ReglaIncumplida;
import java.security.Principal;
import java.time.Clock;
import com.estacionnexo.shared.Periodo;
import java.time.LocalDate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/asistencia")
public class AsistenciaController {
    private final AsistenciaService asistencia;
    private final Clock reloj;

    public AsistenciaController(AsistenciaService asistencia, Clock reloj) {
        this.asistencia = asistencia;
        this.reloj = reloj;
    }

    @GetMapping("/propia")
    String propia(Principal principal, @RequestParam(required = false) LocalDate desde,
                  @RequestParam(required = false) LocalDate hasta,
                  @RequestParam(defaultValue = "0") int pagina, Model model) {
        var periodo = periodo(desde, hasta, model);
        desde = periodo.desde();
        hasta = periodo.hasta();
        model.addAttribute("jornadas", asistencia.propia(principal.getName(), desde, hasta, pagina));
        model.addAttribute("resumen", asistencia.resumen(principal.getName(), desde, hasta));
        return vista(false, desde, hasta, null, pagina, model);
    }

    @GetMapping("/personal")
    String personal(Principal principal, @RequestParam(required = false) Long empleado,
                    @RequestParam(required = false) LocalDate desde, @RequestParam(required = false) LocalDate hasta,
                    @RequestParam(defaultValue = "0") int pagina, Model model) {
        var periodo = periodo(desde, hasta, model);
        desde = periodo.desde();
        hasta = periodo.hasta();
        model.addAttribute("resumen", asistencia.resumenPersonal(principal.getName(), empleado, desde, hasta));
        model.addAttribute("jornadas", asistencia.personal(principal.getName(), empleado, desde, hasta, pagina));
        return vista(true, desde, hasta, empleado, pagina, model);
    }

    @PostMapping("/propia")
    String marcar(Principal principal, @RequestParam AsistenciaService.Marca marca, RedirectAttributes redireccion) {
        try {
            asistencia.marcar(principal.getName(), marca);
            redireccion.addFlashAttribute("mensaje", "Marcación registrada.");
        } catch (ReglaIncumplida e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/asistencia/propia";
    }

    private Periodo periodo(LocalDate desde, LocalDate hasta, Model model) {
        try {
            return Periodo.resolver(reloj, desde, hasta);
        } catch (ReglaIncumplida e) {
            model.addAttribute("error", e.getMessage());
            return Periodo.resolver(reloj, null, null);
        }
    }

    private String vista(boolean personal, LocalDate desde, LocalDate hasta, Long empleado, int pagina, Model model) {
        model.addAttribute("personal", personal);
        model.addAttribute("ruta", personal ? "/asistencia/personal" : "/asistencia/propia");
        model.addAttribute("hoy", Periodo.resolver(reloj, null, null).hoy());
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("empleado", empleado);
        model.addAttribute("pagina", pagina);
        return "asistencia/lista";
    }
}
