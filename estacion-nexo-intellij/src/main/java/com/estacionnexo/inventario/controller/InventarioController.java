package com.estacionnexo.inventario.controller;

import com.estacionnexo.auth.model.Cuenta;
import com.estacionnexo.dashboard.service.DashboardService;
import com.estacionnexo.auth.service.CuentaService;
import com.estacionnexo.exception.ReglaIncumplida;
import com.estacionnexo.inventario.service.InventarioService;
import com.estacionnexo.producto.service.ProductoService;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/inventario")
public class InventarioController {
    private final InventarioService inventario;
    private final ProductoService productos;
    private final CuentaService cuentas;
    private final DashboardService dashboard;

    public InventarioController(InventarioService inventario, ProductoService productos, CuentaService cuentas,
                                DashboardService dashboard) {
        this.inventario = inventario;
        this.productos = productos;
        this.cuentas = cuentas;
        this.dashboard = dashboard;
    }

    @GetMapping("/existencias")
    String existencias(@RequestParam(defaultValue = "0") int pagina, Model model) {
        int actual = Math.max(0, pagina);
        model.addAttribute("productos", productos.listar(false, actual, null));
        model.addAttribute("pagina", actual);
        model.addAttribute("inventario", dashboard.inventario());
        return "inventario/existencias";
    }

    @GetMapping({"/entrada", "/salida"})
    String ajuste(HttpServletRequest request, Model model) {
        model.addAttribute("solicitud", UUID.randomUUID());
        return formulario(request, model);
    }

    @PostMapping({"/entrada", "/salida"})
    String ajustar(HttpServletRequest request, Principal principal, @RequestParam UUID solicitud,
                   @RequestParam long producto, @RequestParam BigDecimal litros, @RequestParam String motivo,
                   Model model, RedirectAttributes redireccion) {
        var cuenta = cuentas.buscar(principal.getName()).filter(Cuenta::activa)
            .orElseThrow(() -> new AccessDeniedException("La cuenta no está activa."));
        var tipo = entrada(request) ? InventarioService.Tipo.ENTRADA : InventarioService.Tipo.SALIDA;
        try {
            inventario.ajustar(cuenta.usuario(), producto, tipo, litros, motivo, solicitud);
        } catch (DuplicateKeyException e) {
            model.addAttribute("error", "Esta solicitud ya fue registrada.");
            model.addAttribute("solicitud", solicitud);
            return formulario(request, model);
        } catch (ReglaIncumplida e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("solicitud", solicitud);
            return formulario(request, model);
        }
        redireccion.addFlashAttribute("mensaje", "Movimiento registrado.");
        return "redirect:/inventario/movimientos";
    }

    private String formulario(HttpServletRequest request, Model model) {
        model.addAttribute("entrada", entrada(request));
        return "inventario/ajuste";
    }

    private boolean entrada(HttpServletRequest request) {
        return request.getServletPath().endsWith("/entrada");
    }
}
