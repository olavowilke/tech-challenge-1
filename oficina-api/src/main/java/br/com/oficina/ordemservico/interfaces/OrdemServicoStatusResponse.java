package br.com.oficina.ordemservico.interfaces;

import br.com.oficina.ordemservico.domain.OrdemServico;
import br.com.oficina.ordemservico.domain.StatusOS;

import java.util.UUID;

/**
 * Reduced response for the public status endpoint — exposes only the OS id and
 * its current status, omitting pricing, line items, and any other internal data
 * that clients should not see on an unauthenticated endpoint.
 */
public record OrdemServicoStatusResponse(UUID id, StatusOS status) {

    public static OrdemServicoStatusResponse from(OrdemServico os) {
        return new OrdemServicoStatusResponse(os.getId(), os.getStatus());
    }
}
