package br.com.oficina.servico.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServicoRepository {
    Optional<Servico> findById(UUID id);
    List<Servico> findAll();
    List<Servico> findAllAtivos();
    Servico save(Servico servico);
    boolean existsById(UUID id);
}
