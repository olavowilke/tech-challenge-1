package br.com.oficina.ordemservico.application;

import br.com.oficina.cliente.domain.ClienteRepository;
import br.com.oficina.ordemservico.domain.OrdemServico;
import br.com.oficina.ordemservico.domain.OrdemServicoRepository;
import br.com.oficina.ordemservico.domain.StatusOS;
import br.com.oficina.peca.domain.Peca;
import br.com.oficina.peca.domain.PecaRepository;
import br.com.oficina.servico.domain.Servico;
import br.com.oficina.servico.domain.ServicoRepository;
import br.com.oficina.shared.domain.RecursoNaoEncontradoException;
import br.com.oficina.veiculo.domain.VeiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdemServicoServiceTest {

    @Mock
    private OrdemServicoRepository ordemServicoRepository;

    @Mock
    private ServicoRepository servicoRepository;

    @Mock
    private PecaRepository pecaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private VeiculoRepository veiculoRepository;

    @InjectMocks
    private OrdemServicoService service;

    private UUID clienteId;
    private UUID veiculoId;

    @BeforeEach
    void setUp() {
        clienteId = UUID.randomUUID();
        veiculoId = UUID.randomUUID();
    }

    @Test
    void deveCriarOrdemServico() {
        CriarOrdemServicoCommand command = new CriarOrdemServicoCommand(clienteId, veiculoId, "Revisão");
        when(clienteRepository.existsById(clienteId)).thenReturn(true);
        when(veiculoRepository.existsById(veiculoId)).thenReturn(true);
        when(ordemServicoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrdemServico os = service.criar(command);

        assertThat(os).isNotNull();
        assertThat(os.getStatus()).isEqualTo(StatusOS.RECEBIDA);
        assertThat(os.getClienteId()).isEqualTo(clienteId);
        verify(ordemServicoRepository).save(any());
    }

    @Test
    void deveLancarExcecaoAoCriarOSComClienteInexistente() {
        CriarOrdemServicoCommand command = new CriarOrdemServicoCommand(clienteId, veiculoId, "Revisão");
        when(clienteRepository.existsById(clienteId)).thenReturn(false);

        assertThatThrownBy(() -> service.criar(command))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Cliente");
        verify(ordemServicoRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoCriarOSComVeiculoInexistente() {
        CriarOrdemServicoCommand command = new CriarOrdemServicoCommand(clienteId, veiculoId, "Revisão");
        when(clienteRepository.existsById(clienteId)).thenReturn(true);
        when(veiculoRepository.existsById(veiculoId)).thenReturn(false);

        assertThatThrownBy(() -> service.criar(command))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Veículo");
        verify(ordemServicoRepository, never()).save(any());
    }

    @Test
    void deveAdicionarServico() {
        OrdemServico os = OrdemServico.nova(clienteId, veiculoId, null);
        Servico servico = Servico.reconstituir(UUID.randomUUID(), "Troca de óleo", null,
                new BigDecimal("150.00"), 60, true, null, null);

        when(ordemServicoRepository.findById(os.getId())).thenReturn(Optional.of(os));
        when(servicoRepository.findById(servico.getId())).thenReturn(Optional.of(servico));
        when(ordemServicoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AdicionarServicoCommand command = new AdicionarServicoCommand(os.getId(), servico.getId());
        OrdemServico resultado = service.adicionarServico(command);

        assertThat(resultado.getItensServico()).hasSize(1);
        assertThat(resultado.getItensServico().get(0).getNomeServico()).isEqualTo("Troca de óleo");
    }

    @Test
    void deveLancarExcecaoAoAdicionarServicoInexistente() {
        OrdemServico os = OrdemServico.nova(clienteId, veiculoId, null);
        when(ordemServicoRepository.findById(os.getId())).thenReturn(Optional.of(os));
        when(servicoRepository.findById(any())).thenReturn(Optional.empty());

        AdicionarServicoCommand command = new AdicionarServicoCommand(os.getId(), UUID.randomUUID());
        assertThatThrownBy(() -> service.adicionarServico(command))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void deveAdicionarPecaEDescontarEstoque() {
        OrdemServico os = OrdemServico.nova(clienteId, veiculoId, null);
        Peca peca = Peca.reconstituir(UUID.randomUUID(), "Filtro", null,
                new BigDecimal("50.00"), 10, true, null, null);

        when(ordemServicoRepository.findById(os.getId())).thenReturn(Optional.of(os));
        when(pecaRepository.findById(peca.getId())).thenReturn(Optional.of(peca));
        when(pecaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(ordemServicoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AdicionarPecaCommand command = new AdicionarPecaCommand(os.getId(), peca.getId(), 3);
        OrdemServico resultado = service.adicionarPeca(command);

        assertThat(resultado.getItensPeca()).hasSize(1);
        assertThat(peca.getQuantidadeEstoque()).isEqualTo(7);
    }

    @Test
    void deveLancarExcecaoAoAdicionarPecaSemEstoque() {
        OrdemServico os = OrdemServico.nova(clienteId, veiculoId, null);
        Peca peca = Peca.reconstituir(UUID.randomUUID(), "Filtro", null,
                new BigDecimal("50.00"), 2, true, null, null);

        when(ordemServicoRepository.findById(os.getId())).thenReturn(Optional.of(os));
        when(pecaRepository.findById(peca.getId())).thenReturn(Optional.of(peca));

        AdicionarPecaCommand command = new AdicionarPecaCommand(os.getId(), peca.getId(), 5);
        assertThatThrownBy(() -> service.adicionarPeca(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Estoque insuficiente");
    }

    @Test
    void deveBuscarOrdemServicoPorId() {
        OrdemServico os = OrdemServico.nova(clienteId, veiculoId, null);
        when(ordemServicoRepository.findById(os.getId())).thenReturn(Optional.of(os));

        OrdemServico resultado = service.buscarPorId(os.getId());
        assertThat(resultado.getId()).isEqualTo(os.getId());
    }

    @Test
    void deveLancarExcecaoQuandoOsNaoEncontrada() {
        UUID idInexistente = UUID.randomUUID();
        when(ordemServicoRepository.findById(idInexistente)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(idInexistente))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void deveAvancarStatus() {
        OrdemServico os = OrdemServico.nova(clienteId, veiculoId, null);
        when(ordemServicoRepository.findById(os.getId())).thenReturn(Optional.of(os));
        when(ordemServicoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrdemServico resultado = service.avancarStatus(os.getId(), StatusOS.EM_DIAGNOSTICO);
        assertThat(resultado.getStatus()).isEqualTo(StatusOS.EM_DIAGNOSTICO);
    }

    @Test
    void deveAprovarOrcamento() {
        OrdemServico os = OrdemServico.nova(clienteId, veiculoId, null);
        os.avancarStatus(StatusOS.EM_DIAGNOSTICO);
        os.avancarStatus(StatusOS.AGUARDANDO_APROVACAO);

        when(ordemServicoRepository.findById(os.getId())).thenReturn(Optional.of(os));
        when(ordemServicoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrdemServico resultado = service.aprovarOrcamento(os.getId());
        assertThat(resultado.getStatus()).isEqualTo(StatusOS.EM_EXECUCAO);
    }

    @Test
    void deveRecusarOrcamento() {
        OrdemServico os = OrdemServico.nova(clienteId, veiculoId, null);
        os.avancarStatus(StatusOS.EM_DIAGNOSTICO);
        os.avancarStatus(StatusOS.AGUARDANDO_APROVACAO);

        when(ordemServicoRepository.findById(os.getId())).thenReturn(Optional.of(os));
        when(ordemServicoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrdemServico resultado = service.recusarOrcamento(os.getId());
        assertThat(resultado.getStatus()).isEqualTo(StatusOS.CANCELADA);
    }
}
