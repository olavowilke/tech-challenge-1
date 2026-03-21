package br.com.oficina.ordemservico.interfaces;

import br.com.oficina.ordemservico.domain.ItemServico;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemServicoResponse(UUID id, UUID servicoId, String nomeServico, BigDecimal valorCobrado) {

    public static ItemServicoResponse from(ItemServico item) {
        return new ItemServicoResponse(item.getId(), item.getServicoId(), item.getNomeServico(), item.getValorCobrado());
    }
}
