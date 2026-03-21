package br.com.oficina.ordemservico.interfaces;

import br.com.oficina.ordemservico.domain.OrdemServico;
import br.com.oficina.ordemservico.domain.StatusOS;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrdemServicoResponse(
        UUID id,
        UUID clienteId,
        UUID veiculoId,
        StatusOS status,
        String observacoes,
        List<ItemServicoResponse> itensServico,
        List<ItemPecaResponse> itensPeca,
        BigDecimal orcamentoTotal,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm,
        LocalDateTime inicioExecucao,
        LocalDateTime fimExecucao
) {
    public static OrdemServicoResponse from(OrdemServico os) {
        return new OrdemServicoResponse(
                os.getId(),
                os.getClienteId(),
                os.getVeiculoId(),
                os.getStatus(),
                os.getObservacoes(),
                os.getItensServico().stream().map(ItemServicoResponse::from).toList(),
                os.getItensPeca().stream().map(ItemPecaResponse::from).toList(),
                os.calcularOrcamentoTotal(),
                os.getCriadoEm(),
                os.getAtualizadoEm(),
                os.getInicioExecucao(),
                os.getFimExecucao()
        );
    }
}
