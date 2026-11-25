package com.estacionnexo.api;

import com.estacionnexo.asistencia.service.AsistenciaService;
import com.estacionnexo.auth.model.Cuenta;
import com.estacionnexo.auth.service.CuentaService;
import com.estacionnexo.finanzas.service.CajaService;
import com.estacionnexo.inventario.service.InventarioService;
import com.estacionnexo.shared.operacion.dto.OperacionSolicitud;
import com.estacionnexo.shared.operacion.service.OperacionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class OperacionesApi {
    private final CuentaService cuentas;
    private final OperacionService operaciones;
    private final CajaService caja;
    private final InventarioService inventario;
    private final AsistenciaService asistencia;

    public OperacionesApi(CuentaService cuentas, OperacionService operaciones, CajaService caja,
                          InventarioService inventario, AsistenciaService asistencia) {
        this.cuentas = cuentas;
        this.operaciones = operaciones;
        this.caja = caja;
        this.inventario = inventario;
        this.asistencia = asistencia;
    }

    @GetMapping("/sesion/csrf")
    CsrfToken csrf(CsrfToken token) {
        return token;
    }

    @PostMapping("/compras")
    @ResponseStatus(HttpStatus.CREATED)
    Id comprar(Principal principal, @Valid @RequestBody OperacionSolicitud solicitud) {
        return new Id(operaciones.comprar(actor(principal).usuario(), solicitud));
    }

    @PostMapping("/ventas")
    @ResponseStatus(HttpStatus.CREATED)
    Id vender(Principal principal, @Valid @RequestBody OperacionSolicitud solicitud) {
        return new Id(operaciones.vender(actor(principal).usuario(), solicitud));
    }

    @PostMapping("/inventario/ajustes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void ajustar(Principal principal, @Valid @RequestBody Ajuste ajuste) {
        inventario.ajustar(actor(principal).usuario(), ajuste.producto(), ajuste.tipo(),
            ajuste.litros(), ajuste.motivo(), ajuste.solicitud());
    }

    @PostMapping("/caja/apertura")
    @ResponseStatus(HttpStatus.CREATED)
    Id abrir(Principal principal, @Valid @RequestBody Apertura apertura) {
        return new Id(caja.abrir(actor(principal).usuario(), apertura.saldo()));
    }

    @PostMapping("/caja/cierre")
    Saldo cerrar(Principal principal) {
        return new Saldo(caja.cerrar(actor(principal).usuario()));
    }

    @PostMapping("/asistencia/propia")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void marcar(Principal principal, @Valid @RequestBody Marcacion marcacion) {
        asistencia.marcar(principal.getName(), marcacion.marca());
    }

    @GetMapping("/asistencia/propia")
    java.util.List<com.estacionnexo.asistencia.model.Jornada> propia(Principal principal,
            @RequestParam(required = false) LocalDate desde, @RequestParam(required = false) LocalDate hasta,
            @RequestParam(defaultValue = "0") int pagina) {
        return asistencia.propia(principal.getName(), desde, hasta, pagina);
    }

    @GetMapping("/asistencia/personal")
    java.util.List<com.estacionnexo.asistencia.model.Jornada> personal(Principal principal,
            @RequestParam(required = false) Long empleado, @RequestParam(required = false) LocalDate desde,
            @RequestParam(required = false) LocalDate hasta, @RequestParam(defaultValue = "0") int pagina) {
        return asistencia.personal(principal.getName(), empleado, desde, hasta, pagina);
    }

    private Cuenta actor(Principal principal) {
        return cuentas.buscar(principal.getName()).filter(Cuenta::activa)
            .orElseThrow(() -> new AccessDeniedException("La cuenta no está activa."));
    }

    record Id(long id) {}
    record Saldo(BigDecimal saldo) {}
    record Apertura(@NotNull @PositiveOrZero BigDecimal saldo) {}
    record Marcacion(@NotNull AsistenciaService.Marca marca) {}
    record Ajuste(@Positive long producto, @NotNull InventarioService.Tipo tipo,
                  @NotNull @Positive BigDecimal litros, @NotBlank @Size(max = 200) String motivo,
                  @NotNull UUID solicitud) {}
}
