package br.com.oficina.peca.infrastructure;

import br.com.oficina.peca.domain.Peca;

public class PecaMapper {

    private PecaMapper() {}

    public static PecaEntity toEntity(Peca peca) {
        PecaEntity entity = new PecaEntity();
        entity.setId(peca.getId());
        entity.setNome(peca.getNome());
        entity.setDescricao(peca.getDescricao());
        entity.setPrecoUnitario(peca.getPrecoUnitario());
        entity.setQuantidadeEstoque(peca.getQuantidadeEstoque());
        entity.setAtivo(peca.isAtivo());
        entity.setCriadoEm(peca.getCriadoEm());
        entity.setAtualizadoEm(peca.getAtualizadoEm());
        return entity;
    }

    public static Peca toDomain(PecaEntity entity) {
        return Peca.reconstituir(
                entity.getId(),
                entity.getNome(),
                entity.getDescricao(),
                entity.getPrecoUnitario(),
                entity.getQuantidadeEstoque(),
                entity.isAtivo(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm()
        );
    }
}
