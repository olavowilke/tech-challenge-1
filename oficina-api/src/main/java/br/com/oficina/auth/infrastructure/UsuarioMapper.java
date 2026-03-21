package br.com.oficina.auth.infrastructure;

import br.com.oficina.auth.domain.Usuario;

public class UsuarioMapper {

    private UsuarioMapper() {}

    public static UsuarioEntity toEntity(Usuario usuario) {
        UsuarioEntity e = new UsuarioEntity();
        e.setId(usuario.getId());
        e.setUsername(usuario.getUsername());
        e.setPasswordHash(usuario.getPasswordHash());
        e.setRole(usuario.getRole());
        e.setAtivo(usuario.isAtivo());
        e.setCriadoEm(usuario.getCriadoEm());
        return e;
    }

    public static Usuario toDomain(UsuarioEntity e) {
        return Usuario.reconstituir(e.getId(), e.getUsername(), e.getPasswordHash(),
                e.getRole(), e.isAtivo(), e.getCriadoEm());
    }
}
