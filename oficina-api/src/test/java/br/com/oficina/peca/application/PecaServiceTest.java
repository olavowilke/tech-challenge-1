package br.com.oficina.peca.application;

import br.com.oficina.peca.domain.Peca;
import br.com.oficina.peca.domain.PecaRepository;
import br.com.oficina.shared.domain.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PecaServiceTest {

    @Mock
    private PecaRepository pecaRepository;

    @InjectMocks
    private PecaService pecaService;

    private Peca buildPeca() {
        return Peca.novo("Filtro de Óleo", "Filtro para motor", new BigDecimal("25.90"), 10);
    }

    @Test
    void cadastrarShouldSaveAndReturnPeca() {
        CadastrarPecaCommand command = new CadastrarPecaCommand(
                "Filtro de Óleo", "Filtro para motor", new BigDecimal("25.90"), 10);
        when(pecaRepository.save(any(Peca.class))).thenAnswer(inv -> inv.getArgument(0));

        Peca result = pecaService.cadastrar(command);

        assertNotNull(result);
        assertEquals("Filtro de Óleo", result.getNome());
        verify(pecaRepository).save(any(Peca.class));
    }

    @Test
    void ajustarEstoqueFindsAdjustsAndSavesAndReturnsPeca() {
        UUID id = UUID.randomUUID();
        Peca peca = buildPeca();
        when(pecaRepository.findById(id)).thenReturn(Optional.of(peca));
        when(pecaRepository.save(any(Peca.class))).thenAnswer(inv -> inv.getArgument(0));

        Peca result = pecaService.ajustarEstoque(id, 5);

        assertEquals(15, result.getQuantidadeEstoque());
        verify(pecaRepository).save(any(Peca.class));
    }

    @Test
    void ajustarEstoqueThrowsRecursoNaoEncontradoExceptionWhenPecaNotFound() {
        UUID id = UUID.randomUUID();
        when(pecaRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () ->
                pecaService.ajustarEstoque(id, 5));
    }

    @Test
    void removerDesativaPecaSoftDelete() {
        UUID id = UUID.randomUUID();
        Peca peca = buildPeca();
        when(pecaRepository.findById(id)).thenReturn(Optional.of(peca));
        when(pecaRepository.save(any(Peca.class))).thenAnswer(inv -> inv.getArgument(0));

        pecaService.remover(id);

        assertFalse(peca.isAtivo());
        verify(pecaRepository).save(peca);
    }

    @Test
    void buscarPorIdThrowsRecursoNaoEncontradoExceptionWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(pecaRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () ->
                pecaService.buscarPorId(id));
    }
}
