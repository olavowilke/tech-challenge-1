package br.com.oficina.servico.application;

import br.com.oficina.servico.domain.Servico;
import br.com.oficina.servico.domain.ServicoRepository;
import br.com.oficina.shared.domain.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicoServiceTest {

    @Mock
    private ServicoRepository servicoRepository;

    @InjectMocks
    private ServicoService servicoService;

    private Servico buildServico() {
        return Servico.novo("Troca de Óleo", "Troca completa de óleo", new BigDecimal("150.00"), 30);
    }

    @Test
    void cadastrarShouldSaveAndReturnServico() {
        CadastrarServicoCommand command = new CadastrarServicoCommand(
                "Troca de Óleo", "Troca completa de óleo", new BigDecimal("150.00"), 30);
        when(servicoRepository.save(any(Servico.class))).thenAnswer(inv -> inv.getArgument(0));

        Servico result = servicoService.cadastrar(command);

        assertNotNull(result);
        assertEquals("Troca de Óleo", result.getNome());
        verify(servicoRepository).save(any(Servico.class));
    }

    @Test
    void atualizarShouldUpdateAndSave() {
        UUID id = UUID.randomUUID();
        Servico servico = buildServico();
        when(servicoRepository.findById(id)).thenReturn(Optional.of(servico));
        when(servicoRepository.save(any(Servico.class))).thenAnswer(inv -> inv.getArgument(0));

        AtualizarServicoCommand command = new AtualizarServicoCommand(
                "Troca de Óleo Sintético", "Óleo sintético premium", new BigDecimal("200.00"), 45);
        Servico result = servicoService.atualizar(id, command);

        assertEquals("Troca de Óleo Sintético", result.getNome());
        verify(servicoRepository).save(any(Servico.class));
    }

    @Test
    void atualizarShouldThrowRecursoNaoEncontradoExceptionWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(servicoRepository.findById(id)).thenReturn(Optional.empty());

        AtualizarServicoCommand command = new AtualizarServicoCommand(
                "Troca de Óleo", "desc", new BigDecimal("150.00"), 30);
        assertThrows(RecursoNaoEncontradoException.class, () ->
                servicoService.atualizar(id, command));
    }

    @Test
    void removerShouldDesativarServico() {
        UUID id = UUID.randomUUID();
        Servico servico = buildServico();
        when(servicoRepository.findById(id)).thenReturn(Optional.of(servico));
        when(servicoRepository.save(any(Servico.class))).thenAnswer(inv -> inv.getArgument(0));

        servicoService.remover(id);

        assertFalse(servico.isAtivo());
        verify(servicoRepository).save(servico);
    }

    @Test
    void listarShouldReturnAll() {
        Servico s1 = buildServico();
        when(servicoRepository.findAll()).thenReturn(List.of(s1));

        List<Servico> result = servicoService.listar();

        assertEquals(1, result.size());
        verify(servicoRepository).findAll();
    }
}
