package br.com.oficina.auth.domain;

import java.util.Optional;

public interface UsuarioRepository {

    Usuario save(Usuario usuario);

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);
}
