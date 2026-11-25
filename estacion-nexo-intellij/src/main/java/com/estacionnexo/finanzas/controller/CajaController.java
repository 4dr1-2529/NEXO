package com.estacionnexo.finanzas.controller;

import com.estacionnexo.auth.model.Cuenta;
import com.estacionnexo.auth.service.CuentaService;
import com.estacionnexo.dashboard.service.DashboardService;
import com.estacionnexo.exception.ReglaIncumplida;
import com.estacionnexo.finanzas.service.CajaService;
import com.estacionnexo.finanzas.service.ConceptoService;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/finanzas")
public class CajaController {
    private final CajaService caja;
    private final DashboardService dashboard;
    private final CuentaService cuentas;
    private final ConceptoService conceptos;

    public CajaController(CajaService caja, DashboardService dashboard, CuentaService cuentas,
                          ConceptoService conceptos) {
        this.caja = caja;
        this.dashboard = dashboard;
        this.cuentas = cuentas;
        this.conceptos = conceptos;
    }

    @GetMapping
    String resumen(Principal principal, @RequestParam(required = false) LocalDate desde,
                   @RequestParam(required = false) LocalDate hasta, Model model) {
        var cuenta = actor(principal);
        try {
            model.addAttribute("resumen", dashboard.cargar(cuenta, desde, hasta));
        } catch (ReglaIncumplida e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("resumen", dashboard.cargar(cuenta, null, null));
        }
        model.addAttribute("cajaActual", caja.actual().orElse(null));
        return "finanzas/caja";
    }

    @PostMapping("/abrir")
    String abrir(Principal principal, @RequestParam BigDecimal apertura, RedirectAttributes redireccion) {
        try {
            caja.abrir(actor(principal).usuario(), apertura);
            redireccion.addFlashAttribute("mensaje", "La caja del día está abierta.");
        } catch (ReglaIncumplida e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/finanzas";
    }

    @PostMapping("/cerrar")
    String cerrar(Principal principal, RedirectAttributes redireccion) {
        try {
            var saldo = caja.cerrar(actor(principal).usuario());
            redireccion.addFlashAttribute("mensaje", "Caja cerrada con saldo S/ " + saldo);
        } catch (ReglaIncumplida e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/finanzas";
    }

    @GetMapping("/movimiento-manual")
    String formularioManual(Model model) {
        model.addAttribute("solicitud", UUID.randomUUID());
        model.addAttribute("conceptos", conceptos.listar(0).stream()
            .filter(c -> c.activo()
                && !"COMPRA".equals(c.datos().codigo())
                && !"VENTA".equals(c.datos().codigo()))
            .toList());
        return "finanzas/movimiento-manual";
    }

    @PostMapping("/movimiento-manual")
    String registrarManual(Principal principal, @RequestParam long concepto, @RequestParam BigDecimal monto,
                           @RequestParam String descripcion, @RequestParam UUID solicitud,
                           Model model, RedirectAttributes redireccion) {
        try {
            caja.movimientoManual(actor(principal).usuario(), concepto, monto, descripcion, solicitud);
            redireccion.addFlashAttribute("mensaje", "Movimiento manual registrado.");
            return "redirect:/finanzas";
        } catch (ReglaIncumplida e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("solicitud", solicitud);
            model.addAttribute("conceptos", conceptos.listar(0).stream()
                .filter(c -> c.activo()
                    && !"COMPRA".equals(c.datos().codigo())
                    && !"VENTA".equals(c.datos().codigo()))
                .toList());
            return "finanzas/movimiento-manual";
        }
    }

    private Cuenta actor(Principal principal) {
        return cuentas.buscar(principal.getName()).filter(Cuenta::activa)
            .orElseThrow(() -> new AccessDeniedException("La cuenta no está activa."));
    }
}
