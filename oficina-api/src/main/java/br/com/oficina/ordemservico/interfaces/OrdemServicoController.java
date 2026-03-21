package br.com.oficina.ordemservico.interfaces;

import br.com.oficina.ordemservico.application.AdicionarPecaCommand;
import br.com.oficina.ordemservico.application.AdicionarServicoCommand;
import br.com.oficina.ordemservico.application.CriarOrdemServicoCommand;
import br.com.oficina.ordemservico.application.OrdemServicoService;
import br.com.oficina.ordemservico.domain.OrdemServico;
import br.com.oficina.ordemservico.domain.StatusOS;
import br.com.oficina.shared.domain.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.OptionalDouble;
import java.util.UUID;

@RestController
@Tag(name = "Ordens de Serviço", description = "Criação e gestão do ciclo de vida das ordens de serviço")
public class OrdemServicoController {

    private final OrdemServicoService ordemServicoService;

    public OrdemServicoController(OrdemServicoService ordemServicoService) {
        this.ordemServicoService = ordemServicoService;
    }

    @Operation(summary = "Criar OS", description = "Cria uma nova ordem de serviço com status RECEBIDA")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "OS criada",
                    content = @Content(schema = @Schema(implementation = OrdemServicoResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "422", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/ordens-servico")
    public ResponseEntity<OrdemServicoResponse> criar(@Valid @RequestBody CriarOrdemServicoRequest request) {
        CriarOrdemServicoCommand command = new CriarOrdemServicoCommand(
                request.clienteId(), request.veiculoId(), request.observacoes());
        OrdemServico os = ordemServicoService.criar(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(OrdemServicoResponse.from(os));
    }

    @Operation(summary = "Listar OSs",
            description = "Lista todas as ordens. Filtra por `clienteId` ou `status` via query params")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de OSs",
                    content = @Content(schema = @Schema(implementation = OrdemServicoResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/ordens-servico")
    public ResponseEntity<List<OrdemServicoResponse>> listar(
            @RequestParam(required = false) UUID clienteId,
            @RequestParam(required = false) StatusOS status) {
        List<OrdemServico> lista;
        if (clienteId != null) {
            lista = ordemServicoService.listarPorCliente(clienteId);
        } else if (status != null) {
            lista = ordemServicoService.listarPorStatus(status);
        } else {
            lista = ordemServicoService.listar();
        }
        return ResponseEntity.ok(lista.stream().map(OrdemServicoResponse::from).toList());
    }

    @Operation(summary = "Buscar OS por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OS encontrada",
                    content = @Content(schema = @Schema(implementation = OrdemServicoResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "OS não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/ordens-servico/{id}")
    public ResponseEntity<OrdemServicoResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(OrdemServicoResponse.from(ordemServicoService.buscarPorId(id)));
    }

    @Operation(summary = "Consultar status da OS (público)",
            description = "Endpoint público — não requer autenticação. Permite o cliente consultar o status da sua OS")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status da OS",
                    content = @Content(schema = @Schema(implementation = OrdemServicoResponse.class))),
            @ApiResponse(responseCode = "404", description = "OS não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @SecurityRequirements
    @GetMapping("/public/ordens-servico/{id}/status")
    public ResponseEntity<OrdemServicoResponse> consultarStatus(@PathVariable UUID id) {
        return ResponseEntity.ok(OrdemServicoResponse.from(ordemServicoService.buscarPorId(id)));
    }

    @Operation(summary = "Adicionar serviço à OS",
            description = "Vincula um serviço do catálogo à OS. O preço é fixado no momento da adição")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Serviço adicionado",
                    content = @Content(schema = @Schema(implementation = OrdemServicoResponse.class))),
            @ApiResponse(responseCode = "400", description = "OS não permite edição no status atual",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "OS ou serviço não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/ordens-servico/{id}/servicos")
    public ResponseEntity<OrdemServicoResponse> adicionarServico(@PathVariable UUID id,
                                                                  @Valid @RequestBody AdicionarServicoRequest request) {
        AdicionarServicoCommand command = new AdicionarServicoCommand(id, request.servicoId());
        return ResponseEntity.ok(OrdemServicoResponse.from(ordemServicoService.adicionarServico(command)));
    }

    @Operation(summary = "Remover serviço da OS")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Serviço removido",
                    content = @Content(schema = @Schema(implementation = OrdemServicoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Item não encontrado ou OS não editável",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "OS não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/ordens-servico/{id}/servicos/{itemServicoId}")
    public ResponseEntity<OrdemServicoResponse> removerServico(@PathVariable UUID id,
                                                                @PathVariable UUID itemServicoId) {
        return ResponseEntity.ok(OrdemServicoResponse.from(ordemServicoService.removerServico(id, itemServicoId)));
    }

    @Operation(summary = "Adicionar peça à OS",
            description = "Vincula uma peça à OS e desconta do estoque automaticamente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Peça adicionada",
                    content = @Content(schema = @Schema(implementation = OrdemServicoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Estoque insuficiente ou OS não editável",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "OS ou peça não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/ordens-servico/{id}/pecas")
    public ResponseEntity<OrdemServicoResponse> adicionarPeca(@PathVariable UUID id,
                                                               @Valid @RequestBody AdicionarPecaRequest request) {
        AdicionarPecaCommand command = new AdicionarPecaCommand(id, request.pecaId(), request.quantidade());
        return ResponseEntity.ok(OrdemServicoResponse.from(ordemServicoService.adicionarPeca(command)));
    }

    @Operation(summary = "Remover peça da OS",
            description = "Remove a peça da OS e devolve a quantidade ao estoque")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Peça removida",
                    content = @Content(schema = @Schema(implementation = OrdemServicoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Item não encontrado ou OS não editável",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "OS não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/ordens-servico/{id}/pecas/{itemPecaId}")
    public ResponseEntity<OrdemServicoResponse> removerPeca(@PathVariable UUID id,
                                                             @PathVariable UUID itemPecaId) {
        return ResponseEntity.ok(OrdemServicoResponse.from(ordemServicoService.removerPeca(id, itemPecaId)));
    }

    @Operation(summary = "Avançar status da OS",
            description = """
                    Transições permitidas:
                    - RECEBIDA → EM_DIAGNOSTICO
                    - EM_DIAGNOSTICO → AGUARDANDO_APROVACAO
                    - AGUARDANDO_APROVACAO → EM_EXECUCAO ou CANCELADA
                    - EM_EXECUCAO → FINALIZADA
                    - FINALIZADA → ENTREGUE
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status avançado",
                    content = @Content(schema = @Schema(implementation = OrdemServicoResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "OS não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Transição de status inválida",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PatchMapping("/ordens-servico/{id}/status")
    public ResponseEntity<OrdemServicoResponse> avancarStatus(@PathVariable UUID id,
                                                               @Valid @RequestBody AvancarStatusRequest request) {
        return ResponseEntity.ok(OrdemServicoResponse.from(ordemServicoService.avancarStatus(id, request.novoStatus())));
    }

    @Operation(summary = "Aprovar orçamento",
            description = "Aprova o orçamento da OS. Requer status AGUARDANDO_APROVACAO → muda para EM_EXECUCAO")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orçamento aprovado",
                    content = @Content(schema = @Schema(implementation = OrdemServicoResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "OS não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "OS não está aguardando aprovação",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/ordens-servico/{id}/aprovar-orcamento")
    public ResponseEntity<OrdemServicoResponse> aprovarOrcamento(@PathVariable UUID id) {
        return ResponseEntity.ok(OrdemServicoResponse.from(ordemServicoService.aprovarOrcamento(id)));
    }

    @Operation(summary = "Recusar orçamento",
            description = "Recusa o orçamento da OS. Requer status AGUARDANDO_APROVACAO → muda para CANCELADA")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orçamento recusado, OS cancelada",
                    content = @Content(schema = @Schema(implementation = OrdemServicoResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "OS não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "OS não está aguardando aprovação",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/ordens-servico/{id}/recusar-orcamento")
    public ResponseEntity<OrdemServicoResponse> recusarOrcamento(@PathVariable UUID id) {
        return ResponseEntity.ok(OrdemServicoResponse.from(ordemServicoService.recusarOrcamento(id)));
    }

    @Operation(summary = "Tempo médio de execução",
            description = "Retorna o tempo médio (em minutos) de execução das OSs finalizadas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tempo médio calculado",
                    content = @Content(schema = @Schema(implementation = TempoMedioExecucaoResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/ordens-servico/monitoramento/tempo-medio-execucao")
    public ResponseEntity<TempoMedioExecucaoResponse> monitorarTempoMedioExecucao() {
        OptionalDouble media = ordemServicoService.monitorarTempoMedioExecucao();
        TempoMedioExecucaoResponse response = media.isPresent()
                ? TempoMedioExecucaoResponse.comValor(media.getAsDouble())
                : TempoMedioExecucaoResponse.semDados();
        return ResponseEntity.ok(response);
    }
}
