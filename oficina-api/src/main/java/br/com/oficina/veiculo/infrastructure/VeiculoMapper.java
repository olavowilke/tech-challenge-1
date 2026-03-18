package br.com.oficina.veiculo.infrastructure;

import br.com.oficina.veiculo.domain.Placa;
import br.com.oficina.veiculo.domain.Veiculo;

public class VeiculoMapper {

    private VeiculoMapper() {}

    public static VeiculoEntity toEntity(Veiculo veiculo) {
        VeiculoEntity entity = new VeiculoEntity();
        entity.setId(veiculo.getId());
        entity.setClienteId(veiculo.getClienteId());
        entity.setPlaca(veiculo.getPlaca().valor());
        entity.setMarca(veiculo.getMarca());
        entity.setModelo(veiculo.getModelo());
        entity.setAno(veiculo.getAno());
        entity.setCor(veiculo.getCor());
        entity.setCriadoEm(veiculo.getCriadoEm());
        entity.setAtualizadoEm(veiculo.getAtualizadoEm());
        return entity;
    }

    public static Veiculo toDomain(VeiculoEntity entity) {
        Placa placa = new Placa(entity.getPlaca());
        return Veiculo.reconstituir(
                entity.getId(),
                entity.getClienteId(),
                placa,
                entity.getMarca(),
                entity.getModelo(),
                entity.getAno(),
                entity.getCor(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm()
        );
    }
}
