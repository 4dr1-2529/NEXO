package com.estacionnexo.auditoria;

import com.estacionnexo.auditoria.service.AuditoriaService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuditoriaContexto {
    private final AuditoriaService auditoria;

    public AuditoriaContexto(AuditoriaService auditoria) {
        this.auditoria = auditoria;
    }

    public void ok(String accion, String modulo, String registro, String anteriores, String nuevos) {
        registrar(accion, modulo, registro, anteriores, nuevos, "OK");
    }

    public void fallo(String accion, String modulo, String registro, String detalle) {
        registrar(accion, modulo, registro, null, detalle, "FALLIDO");
    }

    public void login(String username, boolean exito, String detalle) {
        auditoria.registrar(username, null, exito ? "LOGIN" : "LOGIN_FALLIDO", "SEGURIDAD",
            username, null, detalle, exito ? "OK" : "FALLIDO");
    }

    private void registrar(String accion, String modulo, String registro, String anteriores,
                           String nuevos, String resultado) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = null;
        String rol = null;
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            username = auth.getName();
            rol = auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5))
                .findFirst()
                .orElse(null);
        }
        auditoria.registrar(username, rol, accion, modulo, registro, anteriores, nuevos, resultado);
    }
}
