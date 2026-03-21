package br.com.oficina.auth.interfaces;

import br.com.oficina.auth.domain.Role;
import br.com.oficina.auth.domain.Usuario;

import java.time.LocalDateTime;
import java.util.UUID;

public record UsuarioResponse(UUID id, String username, Role role, boolean ativo, LocalDateTime criadoEm) {

    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getUsername(),
                usuario.getRole(), usuario.isAtivo(), usuario.getCriadoEm());
    }
}
