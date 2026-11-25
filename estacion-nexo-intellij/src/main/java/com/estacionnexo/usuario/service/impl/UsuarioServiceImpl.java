package com.estacionnexo.usuario.service.impl;

import com.estacionnexo.auditoria.AuditoriaContexto;
import com.estacionnexo.exception.ReglaIncumplida;
import com.estacionnexo.usuario.dto.UsuarioDatos;
import com.estacionnexo.usuario.model.Rol;
import com.estacionnexo.usuario.model.Usuario;
import com.estacionnexo.usuario.repository.UsuarioRepository;
import com.estacionnexo.usuario.service.UsuarioService;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioServiceImpl implements UsuarioService {
    private final UsuarioRepository cuentas;
    private final PasswordEncoder encoder;
    private final Clock reloj;
    private final AuditoriaContexto auditoria;

    public UsuarioServiceImpl(UsuarioRepository cuentas, PasswordEncoder encoder, Clock reloj,
                              AuditoriaContexto auditoria) {
        this.cuentas = cuentas;
        this.encoder = encoder;
        this.reloj = reloj;
        this.auditoria = auditoria;
    }

    @Override
    public List<Usuario> listar(int pagina) {
        return cuentas.listar(25, Math.max(0, pagina) * 25L);
    }

    @Override
    public List<Rol> roles() {
        return cuentas.roles();
    }

    @Override
    public Usuario buscar(long id) {
        return cuentas.buscar(id).orElseThrow(() -> new ReglaIncumplida("Usuario no encontrado."));
    }

    @Override
    @Transactional
    public long crear(UsuarioDatos datos) {
        validarRol(datos.rol());
        if (!cuentas.empleadoActivo(datos.empleado())) {
            throw new ReglaIncumplida("Elige un empleado activo.");
        }
        long id = cuentas.crear(datos, hash(datos.password(), true), LocalDateTime.now(reloj));
        auditoria.ok("CREAR", "USUARIOS", datos.username(), null, "empleado=" + datos.empleado());
        return id;
    }

    @Override
    @Transactional
    public void editar(long id, UsuarioDatos datos, long usuarioActual) {
        var usuario = buscar(id);
        validarRol(datos.rol());
        if (datos.empleado() != usuario.empleado() || (id == usuarioActual && datos.rol() != usuario.rol())) {
            throw new ReglaIncumplida("La cuenta conserva su empleado y no puedes cambiar tu propio rol.");
        }
        cuentas.editar(id, datos, hash(datos.password(), false));
        auditoria.ok("EDITAR", "USUARIOS", datos.username(), "rol=" + usuario.rol(), "rol=" + datos.rol());
    }

    @Override
    @Transactional
    public void cambiarEstado(long id, boolean activo, long usuarioActual) {
        var usuario = buscar(id);
        if (!activo && id == usuarioActual) {
            throw new ReglaIncumplida("No puedes desactivar tu propia cuenta.");
        }
        if (activo && !cuentas.empleadoActivo(usuario.empleado())) {
            throw new ReglaIncumplida("El empleado asociado está inactivo.");
        }
        cuentas.estado(id, activo);
        auditoria.ok(activo ? "ACTIVAR" : "DESACTIVAR", "USUARIOS", usuario.username(),
            null, activo ? "Activo" : "Inactivo");
    }

    private void validarRol(long id) {
        if (roles().stream().noneMatch(rol -> rol.id() == id)) {
            throw new ReglaIncumplida("Selecciona un rol vigente.");
        }
    }

    private String hash(String password, boolean obligatorio) {
        if (!obligatorio && (password == null || password.isEmpty())) {
            return null;
        }
        if (password == null || password.codePointCount(0, password.length()) < 12
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ReglaIncumplida("La contraseña debe contener al menos 12 caracteres y ocupar hasta 72 bytes.");
        }
        return encoder.encode(password);
    }
}
