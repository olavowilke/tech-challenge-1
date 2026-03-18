package br.com.oficina.peca.interfaces;

import br.com.oficina.peca.application.AtualizarPecaCommand;
import br.com.oficina.peca.application.CadastrarPecaCommand;
import br.com.oficina.peca.application.PecaService;
import br.com.oficina.peca.domain.Peca;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/pecas")
@Tag(name = "Peças e Insumos")
public class PecaController {

    private final PecaService pecaService;

    public PecaController(PecaService pecaService) {
        this.pecaService = pecaService;
    }

    @PostMapping
    public ResponseEntity<PecaResponse> cadastrar(@Valid @RequestBody CadastrarPecaRequest request) {
        CadastrarPecaCommand command = new CadastrarPecaCommand(
                request.nome(),
                request.descricao(),
                request.precoUnitario(),
                request.quantidadeEstoqueInicial()
        );
        Peca peca = pecaService.cadastrar(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(PecaResponse.from(peca));
    }

    @GetMapping
    public ResponseEntity<List<PecaResponse>> listar() {
        List<PecaResponse> responses = pecaService.listar().stream()
                .map(PecaResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PecaResponse> buscarPorId(@PathVariable UUID id) {
        Peca peca = pecaService.buscarPorId(id);
        return ResponseEntity.ok(PecaResponse.from(peca));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PecaResponse> atualizar(@PathVariable UUID id,
                                                   @Valid @RequestBody AtualizarPecaRequest request) {
        AtualizarPecaCommand command = new AtualizarPecaCommand(
                request.nome(),
                request.descricao(),
                request.precoUnitario()
        );
        Peca peca = pecaService.atualizar(id, command);
        return ResponseEntity.ok(PecaResponse.from(peca));
    }

    @PatchMapping("/{id}/estoque")
    public ResponseEntity<PecaResponse> ajustarEstoque(@PathVariable UUID id,
                                                        @Valid @RequestBody AjustarEstoqueRequest request) {
        Peca peca = pecaService.ajustarEstoque(id, request.quantidade());
        return ResponseEntity.ok(PecaResponse.from(peca));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        pecaService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
