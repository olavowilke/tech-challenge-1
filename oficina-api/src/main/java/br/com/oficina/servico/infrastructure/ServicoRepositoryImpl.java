package br.com.oficina.servico.infrastructure;

import br.com.oficina.servico.domain.Servico;
import br.com.oficina.servico.domain.ServicoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ServicoRepositoryImpl implements ServicoRepository {

    private final ServicoJpaRepository jpaRepository;

    public ServicoRepositoryImpl(ServicoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Servico> findById(UUID id) {
        return jpaRepository.findById(id).map(ServicoMapper::toDomain);
    }

    @Override
    public List<Servico> findAll() {
        return jpaRepository.findAll().stream()
                .map(ServicoMapper::toDomain)
                .toList();
    }

    @Override
    public List<Servico> findAllAtivos() {
        return jpaRepository.findByAtivoTrue().stream()
                .map(ServicoMapper::toDomain)
                .toList();
    }

    @Override
    public Servico save(Servico servico) {
        ServicoEntity entity = ServicoMapper.toEntity(servico);
        ServicoEntity saved = jpaRepository.save(entity);
        return ServicoMapper.toDomain(saved);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }
}
