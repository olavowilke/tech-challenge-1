package br.com.oficina.peca.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PecaRepository {
    Optional<Peca> findById(UUID id);
    List<Peca> findAll();
    List<Peca> findAllAtivos();
    Peca save(Peca peca);
    boolean existsById(UUID id);
}
