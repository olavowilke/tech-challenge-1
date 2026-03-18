package br.com.oficina.veiculo.interfaces;

import br.com.oficina.veiculo.domain.Veiculo;

import java.time.LocalDateTime;
import java.util.UUID;

public record VeiculoResponse(
        UUID id,
        UUID clienteId,
        String placa,
        String marca,
        String modelo,
        int ano,
        String cor,
        LocalDateTime criadoEm
) {
    public static VeiculoResponse from(Veiculo veiculo) {
        return new VeiculoResponse(
                veiculo.getId(),
                veiculo.getClienteId(),
                veiculo.getPlaca().valor(),
                veiculo.getMarca(),
                veiculo.getModelo(),
                veiculo.getAno(),
                veiculo.getCor(),
                veiculo.getCriadoEm()
        );
    }
}
