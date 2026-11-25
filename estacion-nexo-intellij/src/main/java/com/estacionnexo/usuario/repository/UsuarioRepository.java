package com.estacionnexo.usuario.repository;

import com.estacionnexo.usuario.dto.UsuarioDatos;
import com.estacionnexo.usuario.model.Rol;
import com.estacionnexo.usuario.model.Usuario;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository {
    List<Usuario> listar(int limite, long inicio);
    Optional<Usuario> buscar(long id);
    List<Rol> roles();
    boolean empleadoActivo(long id);
    long crear(UsuarioDatos datos, String hash, LocalDateTime fecha);
    void editar(long id, UsuarioDatos datos, String hash);
    void estado(long id, boolean activo);
}
