package br.com.oficina.peca.application;

import br.com.oficina.peca.domain.Peca;
import br.com.oficina.peca.domain.PecaRepository;
import br.com.oficina.shared.domain.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PecaService {

    private final PecaRepository pecaRepository;

    public PecaService(PecaRepository pecaRepository) {
        this.pecaRepository = pecaRepository;
    }

    public Peca cadastrar(CadastrarPecaCommand command) {
        Peca peca = Peca.novo(command.nome(), command.descricao(), command.precoUnitario(), command.quantidadeEstoqueInicial());
        return pecaRepository.save(peca);
    }

    public Peca atualizar(UUID id, AtualizarPecaCommand command) {
        Peca peca = pecaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Peça", id));
        peca.atualizar(command.nome(), command.descricao(), command.precoUnitario());
        return pecaRepository.save(peca);
    }

    public Peca buscarPorId(UUID id) {
        return pecaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Peça", id));
    }

    public List<Peca> listar() {
        return pecaRepository.findAll();
    }

    public Peca ajustarEstoque(UUID id, int quantidade) {
        Peca peca = pecaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Peça", id));
        peca.ajustarEstoque(quantidade);
        return pecaRepository.save(peca);
    }

    public void remover(UUID id) {
        Peca peca = pecaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Peça", id));
        peca.desativar();
        pecaRepository.save(peca);
    }
}
