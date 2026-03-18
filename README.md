# Oficina API

[![Build Status](https://github.com/olavowilke/tech-challenge-1/actions/workflows/ci.yml/badge.svg)](https://github.com/olavowilke/tech-challenge-1/actions/workflows/ci.yml)
[![Coverage](.github/badges/jacoco.svg)](https://github.com/olavowilke/tech-challenge-1/actions/workflows/ci.yml)


Sistema Integrado de Atendimento e Execução de Serviços para oficina mecânica.
MVP back-end desenvolvido como Tech Challenge da pós-graduação FIAP PosTech.

---

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.2.3 |
| Banco de dados | PostgreSQL 16 |
| Migrations | Flyway 9 |
| Segurança | Spring Security + JWT (JJWT 0.12) |
| Documentação | SpringDoc OpenAPI / Swagger UI |
| Testes | JUnit 5 + Mockito + Testcontainers |
| Cobertura | JaCoCo (mínimo 80% nos domínios críticos) |
| Container | Docker + Docker Compose |

---

## Arquitetura

O projeto adota **Package-by-Feature + DDD**. Cada pacote de feature é um Bounded Context independente com camadas internas:

```
br.com.oficina/
├── cliente/         → domain / application / infrastructure / interfaces
├── veiculo/         → domain / application / infrastructure / interfaces
├── ordemservico/    → domain / application / infrastructure / interfaces
├── servico/         → domain / application / infrastructure / interfaces
├── peca/            → domain / application / infrastructure / interfaces
├── auth/            → domain / application / infrastructure / interfaces
└── shared/          → domínio compartilhado e configurações globais
```

**Regra de dependência:** `interfaces → application → domain`. O `domain` de cada feature não importa Spring, JPA ou outras features.

---

## Pré-requisitos

- Java 21+
- Maven 3.9+ (ou usar o Maven Wrapper incluso: `./mvnw`)
- Docker e Docker Compose

---

## Rodando localmente

### 1. Subir apenas o banco de dados

```bash
docker compose up -d db
```

### 2. Rodar a aplicação

```bash
./mvnw -s settings.xml spring-boot:run
```

A API estará disponível em: `http://localhost:8080/api`

### 3. Rodar tudo via Docker Compose

```bash
docker compose up --build
```

> O `settings.xml` na raiz do projeto sobrepõe configurações de mirror corporativo do Maven. Use-o sempre com o wrapper local.

---

## Variáveis de ambiente

| Variável | Padrão | Descrição |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` | Profile ativo |
| `DB_URL` | `jdbc:postgresql://localhost:5432/oficina_db` | URL do banco |
| `DB_USERNAME` | `oficina` | Usuário do banco |
| `DB_PASSWORD` | `oficina` | Senha do banco |
| `JWT_SECRET` | *(valor de dev)* | Chave secreta JWT (Base64, mín. 256 bits) |
| `JWT_EXPIRATION_MS` | `86400000` | Expiração do token em ms (padrão: 24h) |
| `SERVER_PORT` | `8080` | Porta do servidor |

---

## Documentação da API

Com a aplicação rodando, acesse:

- **Swagger UI:** `http://localhost:8080/api/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/api/v3/api-docs`

---

## Endpoints principais

### Clientes
| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/clientes` | Cadastrar cliente (CPF ou CNPJ) |
| `GET` | `/api/clientes` | Listar clientes |
| `GET` | `/api/clientes/{id}` | Buscar cliente por ID |
| `PUT` | `/api/clientes/{id}` | Atualizar cliente |
| `DELETE` | `/api/clientes/{id}` | Desativar cliente (soft delete) |

### Veículos
| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/veiculos` | Cadastrar veículo |
| `GET` | `/api/veiculos/cliente/{clienteId}` | Listar veículos por cliente |
| `GET` | `/api/veiculos/{id}` | Buscar veículo por ID |
| `PUT` | `/api/veiculos/{id}` | Atualizar veículo |
| `DELETE` | `/api/veiculos/{id}` | Remover veículo |

### Health
| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/health` | Status da aplicação |
| `GET` | `/api/actuator/health` | Health detalhado (DB, disco) |

---

## Rodando os testes

```bash
# Testes + relatório de cobertura
./mvnw -s settings.xml verify

# Somente testes (sem verificação de cobertura mínima)
./mvnw -s settings.xml test
```

O relatório de cobertura JaCoCo é gerado em: `target/site/jacoco/index.html`

> Os testes de integração sobem um container PostgreSQL automaticamente via **Testcontainers** — não é necessário ter o banco rodando localmente para executar os testes.

---

## Justificativa do banco de dados

**PostgreSQL** foi escolhido por:
- Suporte robusto a UUID nativo (tipo `uuid`)
- Confiabilidade e maturidade em produção
- Suporte completo a transações ACID
- Compatibilidade com Flyway e Hibernate/JPA
- Open source com ampla adoção na indústria
