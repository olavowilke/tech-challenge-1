package br.com.oficina.veiculo.application;

public record AtualizarVeiculoCommand(
        String marca,
        String modelo,
        int ano,
        String cor
) {}
