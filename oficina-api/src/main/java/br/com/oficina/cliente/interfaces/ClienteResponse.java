package br.com.oficina.cliente.interfaces;

import br.com.oficina.cliente.domain.Cliente;
import br.com.oficina.cliente.domain.TipoDocumento;

import java.time.LocalDateTime;
import java.util.UUID;

public record ClienteResponse(
        UUID id,
        String nome,
        String email,
        String telefone,
        String documento,
        TipoDocumento tipoDocumento,
        boolean ativo,
        LocalDateTime criadoEm
) {
    public static ClienteResponse from(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getEmail(),
                cliente.getTelefone(),
                cliente.getDocumento().numero(),
                cliente.getDocumento().tipo(),
                cliente.isAtivo(),
                cliente.getCriadoEm()
        );
    }
}
