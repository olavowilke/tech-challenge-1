package br.com.oficina.ordemservico.interfaces;

import br.com.oficina.ordemservico.domain.ItemPeca;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemPecaResponse(UUID id, UUID pecaId, String nomePeca, int quantidade, BigDecimal valorUnitario, BigDecimal subtotal) {

    public static ItemPecaResponse from(ItemPeca item) {
        return new ItemPecaResponse(item.getId(), item.getPecaId(), item.getNomePeca(),
                item.getQuantidade(), item.getValorUnitario(), item.getSubtotal());
    }
}
