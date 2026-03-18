package br.com.oficina.peca.infrastructure;

import br.com.oficina.peca.domain.Peca;
import br.com.oficina.peca.domain.PecaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PecaRepositoryImpl implements PecaRepository {

    private final PecaJpaRepository jpaRepository;

    public PecaRepositoryImpl(PecaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Peca> findById(UUID id) {
        return jpaRepository.findById(id).map(PecaMapper::toDomain);
    }

    @Override
    public List<Peca> findAll() {
        return jpaRepository.findAll().stream()
                .map(PecaMapper::toDomain)
                .toList();
    }

    @Override
    public Peca save(Peca peca) {
        PecaEntity entity = PecaMapper.toEntity(peca);
        PecaEntity saved = jpaRepository.save(entity);
        return PecaMapper.toDomain(saved);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }
}
