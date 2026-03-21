package br.com.oficina.peca.interfaces;

import br.com.oficina.peca.application.AtualizarPecaCommand;
import br.com.oficina.peca.application.CadastrarPecaCommand;
import br.com.oficina.peca.application.PecaService;
import br.com.oficina.peca.domain.Peca;
import br.com.oficina.shared.domain.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/pecas")
@Tag(name = "Peças e Insumos", description = "Gestão do estoque de peças e insumos")
public class PecaController {

    private final PecaService pecaService;

    public PecaController(PecaService pecaService) {
        this.pecaService = pecaService;
    }

    @Operation(summary = "Cadastrar peça", description = "Adiciona uma nova peça ao estoque")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Peça criada",
                    content = @Content(schema = @Schema(implementation = PecaResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "422", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    public ResponseEntity<PecaResponse> cadastrar(@Valid @RequestBody CadastrarPecaRequest request) {
        CadastrarPecaCommand command = new CadastrarPecaCommand(
                request.nome(), request.descricao(), request.precoUnitario(), request.quantidadeEstoqueInicial());
        Peca peca = pecaService.cadastrar(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(PecaResponse.from(peca));
    }

    @Operation(summary = "Listar peças", description = "Retorna todas as peças ativas em estoque")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de peças",
                    content = @Content(schema = @Schema(implementation = PecaResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping
    public ResponseEntity<List<PecaResponse>> listar() {
        return ResponseEntity.ok(pecaService.listar().stream().map(PecaResponse::from).toList());
    }

    @Operation(summary = "Buscar peça por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Peça encontrada",
                    content = @Content(schema = @Schema(implementation = PecaResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Peça não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<PecaResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(PecaResponse.from(pecaService.buscarPorId(id)));
    }

    @Operation(summary = "Atualizar peça", description = "Atualiza nome, descrição e preço unitário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Peça atualizada",
                    content = @Content(schema = @Schema(implementation = PecaResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Peça não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "422", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<PecaResponse> atualizar(@PathVariable UUID id,
                                                   @Valid @RequestBody AtualizarPecaRequest request) {
        AtualizarPecaCommand command = new AtualizarPecaCommand(
                request.nome(), request.descricao(), request.precoUnitario());
        return ResponseEntity.ok(PecaResponse.from(pecaService.atualizar(id, command)));
    }

    @Operation(summary = "Ajustar estoque",
            description = "Ajusta a quantidade em estoque. Use valores positivos para entrada e negativos para saída")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estoque ajustado",
                    content = @Content(schema = @Schema(implementation = PecaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Estoque insuficiente",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Peça não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PatchMapping("/{id}/estoque")
    public ResponseEntity<PecaResponse> ajustarEstoque(@PathVariable UUID id,
                                                        @Valid @RequestBody AjustarEstoqueRequest request) {
        return ResponseEntity.ok(PecaResponse.from(pecaService.ajustarEstoque(id, request.quantidade())));
    }

    @Operation(summary = "Remover peça", description = "Desativa a peça do estoque (soft delete)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Peça removida"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Peça não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        pecaService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
