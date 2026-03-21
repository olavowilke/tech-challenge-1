package br.com.oficina.shared;

import br.com.oficina.auth.interfaces.AuthResponse;
import br.com.oficina.auth.interfaces.LoginRequest;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public abstract class BaseControllerIT {

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
    protected TestRestTemplate restTemplate;

    protected String getAdminToken() {
        LoginRequest loginRequest = new LoginRequest("admin", "admin123");
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/auth/login", loginRequest, AuthResponse.class);
        return response.getBody().token();
    }

    protected HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAdminToken());
        return headers;
    }

    protected <T> ResponseEntity<T> authGet(String url, Class<T> responseType) {
        return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(authHeaders()), responseType);
    }

    protected <T> ResponseEntity<T> authPost(String url, Object body, Class<T> responseType) {
        HttpHeaders headers = authHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), responseType);
    }

    protected <T> ResponseEntity<T> authPut(String url, Object body, Class<T> responseType) {
        HttpHeaders headers = authHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(body, headers), responseType);
    }

    protected <T> ResponseEntity<T> authPatch(String url, Object body, Class<T> responseType) {
        HttpHeaders headers = authHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.exchange(url, HttpMethod.PATCH, new HttpEntity<>(body, headers), responseType);
    }

    protected ResponseEntity<Void> authDelete(String url) {
        return restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(authHeaders()), Void.class);
    }
}
