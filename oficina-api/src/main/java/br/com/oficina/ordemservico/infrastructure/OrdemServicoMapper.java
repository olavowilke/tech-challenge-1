package br.com.oficina.ordemservico.infrastructure;

import br.com.oficina.ordemservico.domain.ItemPeca;
import br.com.oficina.ordemservico.domain.ItemServico;
import br.com.oficina.ordemservico.domain.OrdemServico;

import java.util.List;

public class OrdemServicoMapper {

    private OrdemServicoMapper() {}

    public static OrdemServicoEntity toEntity(OrdemServico os) {
        OrdemServicoEntity entity = new OrdemServicoEntity();
        entity.setId(os.getId());
        entity.setClienteId(os.getClienteId());
        entity.setVeiculoId(os.getVeiculoId());
        entity.setStatus(os.getStatus());
        entity.setObservacoes(os.getObservacoes());
        entity.setCriadoEm(os.getCriadoEm());
        entity.setAtualizadoEm(os.getAtualizadoEm());
        entity.setInicioExecucao(os.getInicioExecucao());
        entity.setFimExecucao(os.getFimExecucao());

        entity.getItensServico().clear();
        for (ItemServico item : os.getItensServico()) {
            ItemServicoEntity itemEntity = new ItemServicoEntity();
            itemEntity.setId(item.getId());
            itemEntity.setOrdemServico(entity);
            itemEntity.setServicoId(item.getServicoId());
            itemEntity.setNomeServico(item.getNomeServico());
            itemEntity.setValorCobrado(item.getValorCobrado());
            entity.getItensServico().add(itemEntity);
        }

        entity.getItensPeca().clear();
        for (ItemPeca item : os.getItensPeca()) {
            ItemPecaEntity itemEntity = new ItemPecaEntity();
            itemEntity.setId(item.getId());
            itemEntity.setOrdemServico(entity);
            itemEntity.setPecaId(item.getPecaId());
            itemEntity.setNomePeca(item.getNomePeca());
            itemEntity.setQuantidade(item.getQuantidade());
            itemEntity.setValorUnitario(item.getValorUnitario());
            entity.getItensPeca().add(itemEntity);
        }

        return entity;
    }

    public static OrdemServico toDomain(OrdemServicoEntity entity) {
        List<ItemServico> itensServico = entity.getItensServico().stream()
                .map(i -> ItemServico.reconstituir(i.getId(), i.getServicoId(), i.getNomeServico(), i.getValorCobrado()))
                .toList();

        List<ItemPeca> itensPeca = entity.getItensPeca().stream()
                .map(i -> ItemPeca.reconstituir(i.getId(), i.getPecaId(), i.getNomePeca(), i.getQuantidade(), i.getValorUnitario()))
                .toList();

        return OrdemServico.reconstituir(
                entity.getId(),
                entity.getClienteId(),
                entity.getVeiculoId(),
                entity.getStatus(),
                entity.getObservacoes(),
                itensServico,
                itensPeca,
                entity.getCriadoEm(),
                entity.getAtualizadoEm(),
                entity.getInicioExecucao(),
                entity.getFimExecucao()
        );
    }
}
