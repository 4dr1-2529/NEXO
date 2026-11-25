package com.estacionnexo.config;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = org.springframework.stereotype.Controller.class)
public class NavegacionWeb {
    @ModelAttribute("navegacion")
    public Navegacion navegacion(HttpServletRequest request) {
        var sesion = SecurityContextHolder.getContext().getAuthentication();
        var ruta = request.getServletPath();
        if (sesion == null || !sesion.isAuthenticated() || sesion instanceof AnonymousAuthenticationToken) {
            return new Navegacion("", List.of());
        }
        var roles = sesion.getAuthorities().stream().map(a -> a.getAuthority()).toList();
        boolean admin = roles.contains("ROLE_ADMINISTRADOR");
        boolean operador = roles.contains("ROLE_OPERADOR");
        boolean vendedor = roles.contains("ROLE_VENDEDOR");
        var grupos = List.of(
            new Grupo("General", List.of(
                new Enlace("Resumen", "/dashboard", ruta.equals("/dashboard")),
                permiso(admin, "Reportes", "/reportes", ruta, ruta.startsWith("/reportes")))),
            new Grupo("Catálogo", List.of(enlace("Categorías", "/categorias", ruta), enlace("Combustibles", "/combustibles", ruta))),
            new Grupo("Operaciones", List.of(enlace("Existencias", "/inventario/existencias", ruta),
                permiso(admin || operador, "Inventario", "/inventario/movimientos", ruta,
                    ruta.startsWith("/inventario/") && !ruta.equals("/inventario/existencias")),
                permiso(admin || vendedor, "Ventas", "/ventas", ruta, ruta.startsWith("/ventas")),
                permiso(admin, "Compras", "/compras", ruta, ruta.startsWith("/compras")),
                permiso(admin, "Proveedores", "/proveedores", ruta, ruta.startsWith("/proveedores")),
                permiso(admin, "Caja", "/finanzas", ruta, ruta.startsWith("/finanzas")))),
            new Grupo("Personal", List.of(enlace("Mi asistencia", "/asistencia/propia", ruta),
                permiso(admin, "Asistencia del personal", "/asistencia/personal", ruta, ruta.startsWith("/asistencia/personal")),
                permiso(admin, "Empleados", "/empleados", ruta, ruta.startsWith("/empleados")),
                permiso(admin, "Usuarios", "/usuarios", ruta, ruta.startsWith("/usuarios")))));
        return new Navegacion(sesion.getName(), grupos);
    }

    @ModelAttribute("envio")
    public String envio() {
        return UUID.randomUUID().toString();
    }

    private Enlace enlace(String nombre, String destino, String ruta) {
        return new Enlace(nombre, destino, ruta.equals(destino) || ruta.startsWith(destino + "/"));
    }

    private Enlace permiso(boolean permitido, String nombre, String destino, String ruta, boolean activo) {
        return new Enlace(nombre, permitido ? destino : "", activo && permitido);
    }

    public record Navegacion(String usuario, List<Grupo> grupos) {}
    public record Grupo(String nombre, List<Enlace> enlaces) {}
    public record Enlace(String nombre, String destino, boolean activo) {}
}
