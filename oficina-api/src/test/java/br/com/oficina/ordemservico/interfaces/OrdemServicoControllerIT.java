package br.com.oficina.ordemservico.interfaces;

import br.com.oficina.cliente.domain.TipoDocumento;
import br.com.oficina.cliente.interfaces.CadastrarClienteRequest;
import br.com.oficina.cliente.interfaces.ClienteResponse;
import br.com.oficina.ordemservico.domain.StatusOS;
import br.com.oficina.peca.interfaces.CadastrarPecaRequest;
import br.com.oficina.peca.interfaces.PecaResponse;
import br.com.oficina.servico.interfaces.CadastrarServicoRequest;
import br.com.oficina.servico.interfaces.ServicoResponse;
import br.com.oficina.veiculo.interfaces.CadastrarVeiculoRequest;
import br.com.oficina.veiculo.interfaces.VeiculoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class OrdemServicoControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("oficina_test")
            .withUsername("oficina")
            .withPassword("oficina");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    private UUID clienteId;
    private UUID veiculoId;
    private UUID servicoId;
    private UUID pecaId;

    @BeforeEach
    void setUp() {
        // Criar cliente
        CadastrarClienteRequest clienteReq = new CadastrarClienteRequest(
                "João Silva", "joao@email.com", "11999999999", "529.982.247-25", TipoDocumento.CPF);
        ResponseEntity<ClienteResponse> clienteRes = restTemplate.postForEntity("/clientes", clienteReq, ClienteResponse.class);
        clienteId = clienteRes.getBody().id();

        // Criar veículo
        CadastrarVeiculoRequest veiculoReq = new CadastrarVeiculoRequest(
                clienteId, "ABC-1234", "Toyota", "Corolla", 2020, "Prata");
        ResponseEntity<VeiculoResponse> veiculoRes = restTemplate.postForEntity("/veiculos", veiculoReq, VeiculoResponse.class);
        veiculoId = veiculoRes.getBody().id();

        // Criar serviço
        CadastrarServicoRequest servicoReq = new CadastrarServicoRequest(
                "Troca de óleo", "Óleo 5W30", new BigDecimal("150.00"), 30);
        ResponseEntity<ServicoResponse> servicoRes = restTemplate.postForEntity("/servicos", servicoReq, ServicoResponse.class);
        servicoId = servicoRes.getBody().id();

        // Criar peça
        CadastrarPecaRequest pecaReq = new CadastrarPecaRequest(
                "Filtro de Óleo", "Filtro original", new BigDecimal("35.00"), 20);
        ResponseEntity<PecaResponse> pecaRes = restTemplate.postForEntity("/pecas", pecaReq, PecaResponse.class);
        pecaId = pecaRes.getBody().id();
    }

    private OrdemServicoResponse criarOS() {
        CriarOrdemServicoRequest request = new CriarOrdemServicoRequest(clienteId, veiculoId, "Revisão completa");
        ResponseEntity<OrdemServicoResponse> response = restTemplate.postForEntity(
                "/ordens-servico", request, OrdemServicoResponse.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        return response.getBody();
    }

    @Test
    void shouldReturn201WhenCreatingOS() {
        OrdemServicoResponse os = criarOS();
        assertNotNull(os);
        assertEquals(StatusOS.RECEBIDA, os.status());
        assertEquals(clienteId, os.clienteId());
        assertEquals(veiculoId, os.veiculoId());
    }

    @Test
    void shouldReturn200WhenListingOS() {
        criarOS();
        ResponseEntity<OrdemServicoResponse[]> response = restTemplate.getForEntity(
                "/ordens-servico", OrdemServicoResponse[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0);
    }

    @Test
    void shouldReturn200WhenGettingOSById() {
        OrdemServicoResponse created = criarOS();
        ResponseEntity<OrdemServicoResponse> response = restTemplate.getForEntity(
                "/ordens-servico/" + created.id(), OrdemServicoResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(created.id(), response.getBody().id());
    }

    @Test
    void shouldReturn200WhenAddingServicoToOS() {
        OrdemServicoResponse os = criarOS();
        AdicionarServicoRequest request = new AdicionarServicoRequest(servicoId);
        HttpEntity<AdicionarServicoRequest> entity = new HttpEntity<>(request);

        ResponseEntity<OrdemServicoResponse> response = restTemplate.exchange(
                "/ordens-servico/" + os.id() + "/servicos", HttpMethod.POST, entity, OrdemServicoResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().itensServico().size());
        assertEquals(new BigDecimal("150.00"), response.getBody().orcamentoTotal());
    }

    @Test
    void shouldReturn200WhenAddingPecaToOS() {
        OrdemServicoResponse os = criarOS();
        AdicionarPecaRequest request = new AdicionarPecaRequest(pecaId, 2);
        HttpEntity<AdicionarPecaRequest> entity = new HttpEntity<>(request);

        ResponseEntity<OrdemServicoResponse> response = restTemplate.exchange(
                "/ordens-servico/" + os.id() + "/pecas", HttpMethod.POST, entity, OrdemServicoResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().itensPeca().size());
        assertEquals(new BigDecimal("70.00"), response.getBody().orcamentoTotal());
    }

    @Test
    void shouldExecuteFullFlowFromRecebidaToEntregue() {
        OrdemServicoResponse os = criarOS();
        UUID osId = os.id();

        // RECEBIDA -> EM_DIAGNOSTICO
        patchStatus(osId, StatusOS.EM_DIAGNOSTICO);

        // EM_DIAGNOSTICO -> AGUARDANDO_APROVACAO
        patchStatus(osId, StatusOS.AGUARDANDO_APROVACAO);

        // Aprovar orçamento -> EM_EXECUCAO
        ResponseEntity<OrdemServicoResponse> aprovado = restTemplate.postForEntity(
                "/ordens-servico/" + osId + "/aprovar-orcamento", null, OrdemServicoResponse.class);
        assertEquals(HttpStatus.OK, aprovado.getStatusCode());
        assertEquals(StatusOS.EM_EXECUCAO, aprovado.getBody().status());
        assertNotNull(aprovado.getBody().inicioExecucao());

        // EM_EXECUCAO -> FINALIZADA
        OrdemServicoResponse finalizada = patchStatus(osId, StatusOS.FINALIZADA);
        assertEquals(StatusOS.FINALIZADA, finalizada.status());
        assertNotNull(finalizada.fimExecucao());

        // FINALIZADA -> ENTREGUE
        OrdemServicoResponse entregue = patchStatus(osId, StatusOS.ENTREGUE);
        assertEquals(StatusOS.ENTREGUE, entregue.status());
    }

    @Test
    void shouldReturn400WhenInvalidStatusTransition() {
        OrdemServicoResponse os = criarOS();
        AvancarStatusRequest request = new AvancarStatusRequest(StatusOS.FINALIZADA);
        HttpEntity<AvancarStatusRequest> entity = new HttpEntity<>(request);

        ResponseEntity<Object> response = restTemplate.exchange(
                "/ordens-servico/" + os.id() + "/status", HttpMethod.PATCH, entity, Object.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void shouldReturn200WhenRecusandoOrcamento() {
        OrdemServicoResponse os = criarOS();
        UUID osId = os.id();

        patchStatus(osId, StatusOS.EM_DIAGNOSTICO);
        patchStatus(osId, StatusOS.AGUARDANDO_APROVACAO);

        ResponseEntity<OrdemServicoResponse> recusado = restTemplate.postForEntity(
                "/ordens-servico/" + osId + "/recusar-orcamento", null, OrdemServicoResponse.class);

        assertEquals(HttpStatus.OK, recusado.getStatusCode());
        assertEquals(StatusOS.CANCELADA, recusado.getBody().status());
    }

    @Test
    void shouldReturn200WhenConsultingPublicStatus() {
        OrdemServicoResponse os = criarOS();
        ResponseEntity<OrdemServicoResponse> response = restTemplate.getForEntity(
                "/public/ordens-servico/" + os.id() + "/status", OrdemServicoResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(os.id(), response.getBody().id());
    }

    @Test
    void shouldReturn200WhenGettingTempoMedioExecucao() {
        ResponseEntity<TempoMedioExecucaoResponse> response = restTemplate.getForEntity(
                "/ordens-servico/monitoramento/tempo-medio-execucao", TempoMedioExecucaoResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    private OrdemServicoResponse patchStatus(UUID osId, StatusOS status) {
        AvancarStatusRequest request = new AvancarStatusRequest(status);
        HttpEntity<AvancarStatusRequest> entity = new HttpEntity<>(request);
        ResponseEntity<OrdemServicoResponse> response = restTemplate.exchange(
                "/ordens-servico/" + osId + "/status", HttpMethod.PATCH, entity, OrdemServicoResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        return response.getBody();
    }
}
