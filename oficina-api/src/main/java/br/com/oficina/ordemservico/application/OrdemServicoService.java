package br.com.oficina.ordemservico.application;

import br.com.oficina.cliente.domain.ClienteRepository;
import br.com.oficina.ordemservico.domain.*;
import br.com.oficina.peca.domain.Peca;
import br.com.oficina.peca.domain.PecaRepository;
import br.com.oficina.servico.domain.Servico;
import br.com.oficina.servico.domain.ServicoRepository;
import br.com.oficina.shared.domain.RecursoNaoEncontradoException;
import br.com.oficina.veiculo.domain.VeiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.OptionalDouble;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class OrdemServicoService {

    private final OrdemServicoRepository ordemServicoRepository;
    private final ServicoRepository servicoRepository;
    private final PecaRepository pecaRepository;
    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;

    public OrdemServicoService(OrdemServicoRepository ordemServicoRepository,
                               ServicoRepository servicoRepository,
                               PecaRepository pecaRepository,
                               ClienteRepository clienteRepository,
                               VeiculoRepository veiculoRepository) {
        this.ordemServicoRepository = ordemServicoRepository;
        this.servicoRepository = servicoRepository;
        this.pecaRepository = pecaRepository;
        this.clienteRepository = clienteRepository;
        this.veiculoRepository = veiculoRepository;
    }

    @Transactional
    public OrdemServico criar(CriarOrdemServicoCommand command) {
        if (!clienteRepository.existsById(command.clienteId())) {
            throw new RecursoNaoEncontradoException("Cliente", command.clienteId());
        }
        if (!veiculoRepository.existsById(command.veiculoId())) {
            throw new RecursoNaoEncontradoException("Veículo", command.veiculoId());
        }
        OrdemServico os = OrdemServico.nova(command.clienteId(), command.veiculoId(), command.observacoes());
        return ordemServicoRepository.save(os);
    }

    @Transactional
    public OrdemServico adicionarServico(AdicionarServicoCommand command) {
        OrdemServico os = buscarOuLancar(command.ordemServicoId());
        Servico servico = servicoRepository.findById(command.servicoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço", command.servicoId()));
        ItemServico item = ItemServico.novo(servico.getId(), servico.getNome(), servico.getPreco());
        os.adicionarServico(item);
        return ordemServicoRepository.save(os);
    }

    @Transactional
    public OrdemServico removerServico(UUID ordemServicoId, UUID itemServicoId) {
        OrdemServico os = buscarOuLancar(ordemServicoId);
        os.removerServico(itemServicoId);
        return ordemServicoRepository.save(os);
    }

    @Transactional
    public OrdemServico adicionarPeca(AdicionarPecaCommand command) {
        OrdemServico os = buscarOuLancar(command.ordemServicoId());
        Peca peca = pecaRepository.findById(command.pecaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Peça", command.pecaId()));
        peca.ajustarEstoque(-command.quantidade());
        pecaRepository.save(peca);
        ItemPeca item = ItemPeca.novo(peca.getId(), peca.getNome(), command.quantidade(), peca.getPrecoUnitario());
        os.adicionarPeca(item);
        return ordemServicoRepository.save(os);
    }

    @Transactional
    public OrdemServico removerPeca(UUID ordemServicoId, UUID itemPecaId) {
        OrdemServico os = buscarOuLancar(ordemServicoId);
        ItemPeca itemPeca = os.getItensPeca().stream()
                .filter(i -> i.getId().equals(itemPecaId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item de peça não encontrado: " + itemPecaId));
        Peca peca = pecaRepository.findById(itemPeca.getPecaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Peça", itemPeca.getPecaId()));
        peca.ajustarEstoque(itemPeca.getQuantidade());
        pecaRepository.save(peca);
        os.removerPeca(itemPecaId);
        return ordemServicoRepository.save(os);
    }

    @Transactional
    public OrdemServico avancarStatus(UUID ordemServicoId, StatusOS novoStatus) {
        OrdemServico os = buscarOuLancar(ordemServicoId);
        os.avancarStatus(novoStatus);
        return ordemServicoRepository.save(os);
    }

    @Transactional
    public OrdemServico aprovarOrcamento(UUID ordemServicoId) {
        OrdemServico os = buscarOuLancar(ordemServicoId);
        os.aprovarOrcamento();
        return ordemServicoRepository.save(os);
    }

    @Transactional
    public OrdemServico recusarOrcamento(UUID ordemServicoId) {
        OrdemServico os = buscarOuLancar(ordemServicoId);
        os.recusarOrcamento();
        return ordemServicoRepository.save(os);
    }

    public OrdemServico buscarPorId(UUID id) {
        return buscarOuLancar(id);
    }

    public List<OrdemServico> listar() {
        return ordemServicoRepository.findAll();
    }

    public List<OrdemServico> listarPorCliente(UUID clienteId) {
        return ordemServicoRepository.findByClienteId(clienteId);
    }

    public List<OrdemServico> listarPorStatus(StatusOS status) {
        return ordemServicoRepository.findByStatus(status);
    }

    public OptionalDouble monitorarTempoMedioExecucao() {
        return ordemServicoRepository.tempoMedioExecucaoMinutos();
    }

    private OrdemServico buscarOuLancar(UUID id) {
        return ordemServicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de Serviço", id));
    }
}
