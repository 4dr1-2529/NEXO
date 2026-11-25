package com.estacionnexo.shared.consulta.controller;

import com.estacionnexo.auth.model.Cuenta;
import com.estacionnexo.auth.service.CuentaService;
import com.estacionnexo.shared.consulta.repository.ConsultaRepository.Tipo;
import com.estacionnexo.shared.consulta.service.ConsultaService;
import java.security.Principal;
import java.time.LocalDate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ConsultaController {
    private final ConsultaService consultas;
    private final CuentaService cuentas;

    public ConsultaController(ConsultaService consultas, CuentaService cuentas) {
        this.consultas = consultas;
        this.cuentas = cuentas;
    }

    @GetMapping({"/compras", "/ventas", "/inventario/movimientos", "/finanzas/movimientos"})
    String listar(Principal principal, jakarta.servlet.http.HttpServletRequest request,
                  @RequestParam(required = false) LocalDate desde, @RequestParam(required = false) LocalDate hasta,
                  @RequestParam(required = false) String tipo, @RequestParam(defaultValue = "0") int pagina, Model model) {
        var seccion = seccion(request.getServletPath());
        model.addAttribute("registros", consultas.listar(seccion.tipo(), actor(principal), desde, hasta, tipo, pagina));
        model.addAttribute("titulo", seccion.titulo());
        model.addAttribute("ruta", seccion.ruta());
        model.addAttribute("pagina", pagina);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("tipo", tipo);
        model.addAttribute("detalle", seccion.tipo() != Tipo.INVENTARIO);
        return "operaciones/lista";
    }

    @GetMapping({"/compras/{id}", "/ventas/{id}", "/finanzas/movimientos/{id}"})
    String detalle(Principal principal, jakarta.servlet.http.HttpServletRequest request,
                   @PathVariable long id, Model model) {
        var seccion = seccion(request.getServletPath());
        model.addAttribute("resultado", consultas.detalle(seccion.tipo(), actor(principal), id));
        model.addAttribute("ruta", seccion.ruta());
        return "operaciones/detalle";
    }

    private Cuenta actor(Principal principal) {
        return cuentas.buscar(principal.getName()).filter(Cuenta::activa)
            .orElseThrow(() -> new AccessDeniedException("La cuenta no está activa."));
    }

    private Seccion seccion(String ruta) {
        if (ruta.startsWith("/compras")) {
            return new Seccion(Tipo.COMPRAS, "/compras", "Compras");
        }
        if (ruta.startsWith("/ventas")) {
            return new Seccion(Tipo.VENTAS, "/ventas", "Ventas");
        }
        if (ruta.startsWith("/inventario")) {
            return new Seccion(Tipo.INVENTARIO, "/inventario/movimientos", "Movimientos de inventario");
        }
        return new Seccion(Tipo.CAJA, "/finanzas/movimientos", "Ingresos y egresos");
    }

    private record Seccion(Tipo tipo, String ruta, String titulo) {}
}
