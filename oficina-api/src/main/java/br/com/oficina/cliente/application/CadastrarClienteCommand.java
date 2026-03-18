package br.com.oficina.cliente.application;

import br.com.oficina.cliente.domain.TipoDocumento;

public record CadastrarClienteCommand(
        String nome,
        String email,
        String telefone,
        String documento,
        TipoDocumento tipoDocumento
) {}
