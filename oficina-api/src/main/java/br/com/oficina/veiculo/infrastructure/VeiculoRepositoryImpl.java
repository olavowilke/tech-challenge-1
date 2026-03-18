package br.com.oficina.veiculo.infrastructure;

import br.com.oficina.veiculo.domain.Veiculo;
import br.com.oficina.veiculo.domain.VeiculoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class VeiculoRepositoryImpl implements VeiculoRepository {

    private final VeiculoJpaRepository jpaRepository;

    public VeiculoRepositoryImpl(VeiculoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Veiculo> findById(UUID id) {
        return jpaRepository.findById(id).map(VeiculoMapper::toDomain);
    }

    @Override
    public Optional<Veiculo> findByPlaca(String placa) {
        return jpaRepository.findByPlaca(placa).map(VeiculoMapper::toDomain);
    }

    @Override
    public List<Veiculo> findByClienteId(UUID clienteId) {
        return jpaRepository.findByClienteId(clienteId).stream()
                .map(VeiculoMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByPlaca(String placa) {
        return jpaRepository.existsByPlaca(placa);
    }

    @Override
    public Veiculo save(Veiculo veiculo) {
        VeiculoEntity entity = VeiculoMapper.toEntity(veiculo);
        VeiculoEntity saved = jpaRepository.save(entity);
        return VeiculoMapper.toDomain(saved);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }
}
