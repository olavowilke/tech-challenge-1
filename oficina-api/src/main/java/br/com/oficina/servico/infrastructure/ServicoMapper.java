package br.com.oficina.servico.infrastructure;

import br.com.oficina.servico.domain.Servico;

public class ServicoMapper {

    private ServicoMapper() {}

    public static ServicoEntity toEntity(Servico servico) {
        ServicoEntity entity = new ServicoEntity();
        entity.setId(servico.getId());
        entity.setNome(servico.getNome());
        entity.setDescricao(servico.getDescricao());
        entity.setPreco(servico.getPreco());
        entity.setTempoEstimadoMinutos(servico.getTempoEstimadoMinutos());
        entity.setAtivo(servico.isAtivo());
        entity.setCriadoEm(servico.getCriadoEm());
        entity.setAtualizadoEm(servico.getAtualizadoEm());
        return entity;
    }

    public static Servico toDomain(ServicoEntity entity) {
        return Servico.reconstituir(
                entity.getId(),
                entity.getNome(),
                entity.getDescricao(),
                entity.getPreco(),
                entity.getTempoEstimadoMinutos(),
                entity.isAtivo(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm()
        );
    }
}
