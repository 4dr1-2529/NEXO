package com.estacionnexo.usuario.service;

import com.estacionnexo.usuario.dto.UsuarioDatos;
import com.estacionnexo.usuario.model.Rol;
import com.estacionnexo.usuario.model.Usuario;
import java.util.List;

public interface UsuarioService {
    List<Usuario> listar(int pagina);
    List<Rol> roles();
    Usuario buscar(long id);
    long crear(UsuarioDatos datos);
    void editar(long id, UsuarioDatos datos, long usuarioActual);
    void cambiarEstado(long id, boolean activo, long usuarioActual);
}
