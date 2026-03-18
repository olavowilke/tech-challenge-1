package br.com.oficina.peca.interfaces;

import br.com.oficina.peca.domain.Peca;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PecaResponse(
        UUID id,
        String nome,
        String descricao,
        BigDecimal precoUnitario,
        int quantidadeEstoque,
        boolean ativo,
        LocalDateTime criadoEm
) {
    public static PecaResponse from(Peca peca) {
        return new PecaResponse(
                peca.getId(),
                peca.getNome(),
                peca.getDescricao(),
                peca.getPrecoUnitario(),
                peca.getQuantidadeEstoque(),
                peca.isAtivo(),
                peca.getCriadoEm()
        );
    }
}
