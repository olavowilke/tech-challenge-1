package br.com.oficina.veiculo.application;

import java.util.UUID;

public record CadastrarVeiculoCommand(
        UUID clienteId,
        String placa,
        String marca,
        String modelo,
        int ano,
        String cor
) {}
