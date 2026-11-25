package com.estacionnexo.api;

import com.estacionnexo.auth.model.Cuenta;
import com.estacionnexo.auth.service.CuentaService;
import com.estacionnexo.producto.model.Producto;
import com.estacionnexo.producto.service.ProductoService;
import com.estacionnexo.shared.consulta.model.Registro;
import com.estacionnexo.shared.consulta.repository.ConsultaRepository.Tipo;
import com.estacionnexo.shared.consulta.service.ConsultaService;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class ConsultasApi {
    private final ConsultaService consultas;
    private final CuentaService cuentas;
    private final ProductoService productos;

    public ConsultasApi(ConsultaService consultas, CuentaService cuentas, ProductoService productos) {
        this.consultas = consultas;
        this.cuentas = cuentas;
        this.productos = productos;
    }

    @GetMapping({"/compras", "/ventas", "/inventario/movimientos", "/caja/movimientos"})
    List<Registro> listar(Principal principal, jakarta.servlet.http.HttpServletRequest request,
            @RequestParam(required = false) LocalDate desde, @RequestParam(required = false) LocalDate hasta,
            @RequestParam(required = false) String tipo, @RequestParam(defaultValue = "0") int pagina) {
        return consultas.listar(seccion(request.getServletPath()), actor(principal), desde, hasta, tipo, pagina);
    }

    @GetMapping({"/compras/{id}", "/ventas/{id}", "/caja/movimientos/{id}"})
    ConsultaService.Resultado detalle(Principal principal, jakarta.servlet.http.HttpServletRequest request,
                                      @PathVariable long id) {
        return consultas.detalle(seccion(request.getServletPath()), actor(principal), id);
    }

    @GetMapping("/inventario/existencias")
    List<Producto> existencias(@RequestParam(defaultValue = "0") int pagina,
                               @RequestParam(required = false) Long categoria) {
        return productos.listar(false, pagina, categoria);
    }

    private Cuenta actor(Principal principal) {
        return cuentas.buscar(principal.getName()).filter(Cuenta::activa)
            .orElseThrow(() -> new AccessDeniedException("La cuenta no está activa."));
    }

    private Tipo seccion(String ruta) {
        String modulo = ruta.split("/")[3];
        return switch (modulo) {
            case "compras" -> Tipo.COMPRAS;
            case "ventas" -> Tipo.VENTAS;
            case "inventario" -> Tipo.INVENTARIO;
            case "caja" -> Tipo.CAJA;
            default -> throw new IllegalArgumentException("Consulta desconocida.");
        };
    }
}
