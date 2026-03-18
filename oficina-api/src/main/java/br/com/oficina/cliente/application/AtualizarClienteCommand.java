package br.com.oficina.cliente.application;

public record AtualizarClienteCommand(
        String nome,
        String email,
        String telefone
) {}
