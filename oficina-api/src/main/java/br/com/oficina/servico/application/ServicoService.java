package br.com.oficina.servico.application;

import br.com.oficina.servico.domain.Servico;
import br.com.oficina.servico.domain.ServicoRepository;
import br.com.oficina.shared.domain.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ServicoService {

    private final ServicoRepository servicoRepository;

    public ServicoService(ServicoRepository servicoRepository) {
        this.servicoRepository = servicoRepository;
    }

    @Transactional
    public Servico cadastrar(CadastrarServicoCommand command) {
        Servico servico = Servico.novo(command.nome(), command.descricao(), command.preco(), command.tempoEstimadoMinutos());
        return servicoRepository.save(servico);
    }

    @Transactional
    public Servico atualizar(UUID id, AtualizarServicoCommand command) {
        Servico servico = servicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço", id));
        servico.atualizar(command.nome(), command.descricao(), command.preco(), command.tempoEstimadoMinutos());
        return servicoRepository.save(servico);
    }

    public Servico buscarPorId(UUID id) {
        return servicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço", id));
    }

    public List<Servico> listar() {
        return servicoRepository.findAllAtivos();
    }

    @Transactional
    public void remover(UUID id) {
        Servico servico = servicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço", id));
        servico.desativar();
        servicoRepository.save(servico);
    }
}
