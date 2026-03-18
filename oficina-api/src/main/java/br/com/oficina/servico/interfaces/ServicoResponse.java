package br.com.oficina.servico.interfaces;

import br.com.oficina.servico.domain.Servico;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ServicoResponse(
        UUID id,
        String nome,
        String descricao,
        BigDecimal preco,
        int tempoEstimadoMinutos,
        boolean ativo,
        LocalDateTime criadoEm
) {
    public static ServicoResponse from(Servico servico) {
        return new ServicoResponse(
                servico.getId(),
                servico.getNome(),
                servico.getDescricao(),
                servico.getPreco(),
                servico.getTempoEstimadoMinutos(),
                servico.isAtivo(),
                servico.getCriadoEm()
        );
    }
}
