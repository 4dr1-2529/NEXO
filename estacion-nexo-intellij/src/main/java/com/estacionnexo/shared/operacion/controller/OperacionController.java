package com.estacionnexo.shared.operacion.controller;

import com.estacionnexo.auth.model.Cuenta;
import com.estacionnexo.auth.service.CuentaService;
import com.estacionnexo.exception.ReglaIncumplida;
import com.estacionnexo.producto.service.ProductoService;
import com.estacionnexo.proveedor.service.ProveedorService;
import com.estacionnexo.shared.operacion.dto.LineaSolicitud;
import com.estacionnexo.shared.operacion.dto.OperacionSolicitud;
import com.estacionnexo.shared.operacion.service.OperacionService;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class OperacionController {
    private static final int MAX_LINEAS = 50;
    private final OperacionService operaciones;
    private final CuentaService cuentas;
    private final ProductoService productos;
    private final ProveedorService proveedores;

    public OperacionController(OperacionService operaciones, CuentaService cuentas,
                               ProductoService productos, ProveedorService proveedores) {
        this.operaciones = operaciones;
        this.cuentas = cuentas;
        this.productos = productos;
        this.proveedores = proveedores;
    }

    @GetMapping("/ventas/registrar")
    String registrarVenta() {
        return "operaciones/registrar-venta";
    }

    @GetMapping({"/compras/nueva", "/ventas/nueva"})
    String nueva(HttpServletRequest request, Model model) {
        return formulario(esCompra(request), UUID.randomUUID(), null, null, List.of(), model);
    }

    @PostMapping({"/compras/nueva", "/ventas/nueva"})
    String guardar(HttpServletRequest request, Principal principal, @RequestParam UUID solicitud,
                   @RequestParam(required = false) Long proveedor, @RequestParam(required = false) String observacion,
                   @RequestParam(defaultValue = "confirmar") String accion, Model model) {
        boolean compra = esCompra(request);
        var lineas = new ArrayList<LineaFormulario>();
        for (int i = 0; i < MAX_LINEAS && request.getParameter("producto" + i) != null; i++) {
            lineas.add(new LineaFormulario(request.getParameter("producto" + i),
                java.util.Objects.toString(request.getParameter("litros" + i), ""),
                java.util.Objects.toString(request.getParameter("precio" + i), "")));
        }
        if ("agregar".equals(accion)) {
            if (lineas.size() < MAX_LINEAS) {
                lineas.add(new LineaFormulario("", "", ""));
            }
            return formulario(compra, solicitud, proveedor, observacion, lineas, model);
        }
        var actor = cuentas.buscar(principal.getName()).filter(Cuenta::activa)
            .orElseThrow(() -> new AccessDeniedException("La cuenta no está activa."));
        try {
            var datos = lineas.stream().map(linea -> new LineaSolicitud(Long.parseLong(linea.producto()),
                new BigDecimal(linea.litros()), compra ? new BigDecimal(linea.precio()) : null)).toList();
            var entrada = new OperacionSolicitud(solicitud, proveedor, observacion, datos);
            long id = compra ? operaciones.comprar(actor.usuario(), entrada) : operaciones.vender(actor.usuario(), entrada);
            return "redirect:/" + (compra ? "compras" : "ventas") + "/" + id;
        } catch (NumberFormatException e) {
            model.addAttribute("error", "Complete el combustible, los litros y, en compras, el precio de cada línea.");
        } catch (ReglaIncumplida e) {
            model.addAttribute("error", e.getMessage());
        } catch (DuplicateKeyException e) {
            model.addAttribute("error", "La solicitud ya fue procesada o tiene un código repetido.");
        }
        return formulario(compra, solicitud, proveedor, observacion, lineas, model);
    }

    private boolean esCompra(HttpServletRequest request) {
        return request.getServletPath().startsWith("/compras/");
    }

    private String formulario(boolean compra, UUID solicitud, Long proveedor, String observacion,
                              List<LineaFormulario> lineas, Model model) {
        model.addAttribute("compra", compra);
        model.addAttribute("solicitud", solicitud);
        model.addAttribute("proveedor", proveedor);
        model.addAttribute("observacion", observacion);
        model.addAttribute("lineas", lineas.isEmpty() ? List.of(new LineaFormulario("", "", "")) : lineas);
        model.addAttribute("productos", todasLasPaginas(pagina -> productos.listar(false, pagina, null)));
        if (compra) {
            model.addAttribute("proveedores", todasLasPaginas(pagina -> proveedores.listar(false, pagina, null)));
        }
        return "operaciones/formulario";
    }

    private <T> List<T> todasLasPaginas(java.util.function.IntFunction<List<T>> cargar) {
        var registros = new ArrayList<T>();
        for (int pagina = 0; ; pagina++) {
            var lote = cargar.apply(pagina);
            registros.addAll(lote);
            if (lote.size() < 25) {
                return registros;
            }
        }
    }

    public record LineaFormulario(String producto, String litros, String precio) {}
}
