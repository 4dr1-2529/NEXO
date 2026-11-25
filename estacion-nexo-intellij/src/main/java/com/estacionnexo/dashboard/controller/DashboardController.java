package com.estacionnexo.dashboard.controller;

import com.estacionnexo.auth.model.Cuenta;
import com.estacionnexo.auth.service.CuentaService;
import com.estacionnexo.dashboard.service.DashboardService;
import com.estacionnexo.exception.ReglaIncumplida;
import java.security.Principal;
import java.time.LocalDate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DashboardController {
    private final DashboardService dashboard;
    private final CuentaService cuentas;

    public DashboardController(DashboardService dashboard, CuentaService cuentas) {
        this.dashboard = dashboard;
        this.cuentas = cuentas;
    }

    @GetMapping("/dashboard")
    String mostrar(Principal principal, @RequestParam(required = false) LocalDate desde,
                   @RequestParam(required = false) LocalDate hasta, Model model) {
        var cuenta = cuentas.buscar(principal.getName()).filter(Cuenta::activa)
            .orElseThrow(() -> new AccessDeniedException("La cuenta no está activa."));
        model.addAttribute("nombre", cuenta.nombre());
        model.addAttribute("rol", cuenta.rol());
        try {
            model.addAttribute("resumen", dashboard.cargar(cuenta, desde, hasta));
        } catch (ReglaIncumplida e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("resumen", dashboard.cargar(cuenta, null, null));
        }
        return "dashboard";
    }
}
