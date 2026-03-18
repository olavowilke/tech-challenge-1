package br.com.oficina.peca.interfaces;

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
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class PecaControllerIT {

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

    private static final String BASE_URL = "/pecas";

    private CadastrarPecaRequest buildRequest() {
        return new CadastrarPecaRequest(
                "Filtro de Óleo",
                "Filtro para motor",
                new BigDecimal("25.90"),
                10
        );
    }

    @Test
    void shouldReturn201WhenCreatingPecaWithValidRequest() {
        CadastrarPecaRequest request = buildRequest();

        ResponseEntity<PecaResponse> response = restTemplate.postForEntity(BASE_URL, request, PecaResponse.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Filtro de Óleo", response.getBody().nome());
    }

    @Test
    void shouldReturn422WhenCreatingPecaWithBlankNome() {
        CadastrarPecaRequest request = new CadastrarPecaRequest(
                "",
                "desc",
                new BigDecimal("25.90"),
                10
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(BASE_URL, request, Map.class);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    }

    @Test
    void shouldReturn200WhenListingPecas() {
        ResponseEntity<PecaResponse[]> response = restTemplate.getForEntity(BASE_URL, PecaResponse[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void shouldReturn200WhenGettingPecaByIdAfterCreating() {
        CadastrarPecaRequest request = new CadastrarPecaRequest(
                "Vela de Ignição",
                "Vela NGK",
                new BigDecimal("15.50"),
                20
        );

        ResponseEntity<PecaResponse> created = restTemplate.postForEntity(BASE_URL, request, PecaResponse.class);
        assertEquals(HttpStatus.CREATED, created.getStatusCode());
        UUID id = created.getBody().id();

        ResponseEntity<PecaResponse> response = restTemplate.getForEntity(BASE_URL + "/" + id, PecaResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(id, response.getBody().id());
    }

    @Test
    void shouldReturn200WhenAdjustingStockWithPositiveQuantity() {
        CadastrarPecaRequest request = new CadastrarPecaRequest(
                "Correia Dentada",
                "Correia para motor 1.0",
                new BigDecimal("89.90"),
                5
        );

        ResponseEntity<PecaResponse> created = restTemplate.postForEntity(BASE_URL, request, PecaResponse.class);
        assertEquals(HttpStatus.CREATED, created.getStatusCode());
        UUID id = created.getBody().id();

        AjustarEstoqueRequest estoqueRequest = new AjustarEstoqueRequest(10);
        HttpEntity<AjustarEstoqueRequest> entity = new HttpEntity<>(estoqueRequest);
        ResponseEntity<PecaResponse> response = restTemplate.exchange(
                BASE_URL + "/" + id + "/estoque", HttpMethod.PATCH, entity, PecaResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(15, response.getBody().quantidadeEstoque());
    }

    @Test
    void shouldReturn400WhenAdjustingStockThatWouldGoNegative() {
        CadastrarPecaRequest request = new CadastrarPecaRequest(
                "Amortecedor Dianteiro",
                "Amortecedor para eixo dianteiro",
                new BigDecimal("299.90"),
                2
        );

        ResponseEntity<PecaResponse> created = restTemplate.postForEntity(BASE_URL, request, PecaResponse.class);
        assertEquals(HttpStatus.CREATED, created.getStatusCode());
        UUID id = created.getBody().id();

        AjustarEstoqueRequest estoqueRequest = new AjustarEstoqueRequest(-10);
        HttpEntity<AjustarEstoqueRequest> entity = new HttpEntity<>(estoqueRequest);
        ResponseEntity<Map> response = restTemplate.exchange(
                BASE_URL + "/" + id + "/estoque", HttpMethod.PATCH, entity, Map.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldReturn204WhenDeletingPeca() {
        CadastrarPecaRequest request = new CadastrarPecaRequest(
                "Pastilha de Freio",
                "Pastilha traseira",
                new BigDecimal("45.00"),
                8
        );

        ResponseEntity<PecaResponse> created = restTemplate.postForEntity(BASE_URL, request, PecaResponse.class);
        assertEquals(HttpStatus.CREATED, created.getStatusCode());
        UUID id = created.getBody().id();

        ResponseEntity<Void> response = restTemplate.exchange(
                BASE_URL + "/" + id, HttpMethod.DELETE, HttpEntity.EMPTY, Void.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
}
