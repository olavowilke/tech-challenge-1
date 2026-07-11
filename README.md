# Oficina API

[![Build Status](https://github.com/olavowilke/tech-challenge-1/actions/workflows/ci.yml/badge.svg)](https://github.com/olavowilke/tech-challenge-1/actions/workflows/ci.yml)

Sistema Integrado de Atendimento e Execução de Serviços para oficina mecânica.
Back-end desenvolvido como Tech Challenge da pós-graduação FIAP PosTech.

> **Fase 2 — foco em qualidade, resiliência e escalabilidade.** Evolui a Fase 1 com
> refatoração para **Clean Architecture**, novos requisitos funcionais (abertura
> consolidada de OS, listagem ordenada, webhook de aprovação de orçamento,
> notificação por e-mail) e infraestrutura de **Docker → Kubernetes (kind) →
> Terraform → CI/CD (GitHub Actions)** com **autoscaling (HPA)**.

---

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.4.5 |
| Arquitetura | Clean Architecture (Entities / Use Cases / Gateways / Presenters / Controllers) |
| Banco de dados | PostgreSQL 16 |
| Migrations | Flyway |
| Segurança | Spring Security + JWT (JJWT 0.12) |
| Notificações | Spring Mail (SMTP/SendGrid) + webhook de entrada |
| Documentação | SpringDoc OpenAPI / Swagger UI |
| Testes | JUnit 5 + Mockito + Testcontainers + ArchUnit |
| Cobertura | JaCoCo (mínimo 80% nos domínios críticos) |
| Container | Docker + Docker Compose |
| Orquestração | Kubernetes (kind) + HPA + metrics-server |
| IaC | Terraform |
| CI/CD | GitHub Actions (build, testes, imagem GHCR, deploy) |

---

## Arquitetura

O projeto adota **Package-by-Feature + Clean Architecture**. Cada Bounded Context
organiza-se nas camadas da Clean Architecture, com a **regra de dependência apontando
sempre para dentro** — o núcleo (Entities, Use Cases) não conhece Spring nem JPA.
A regra é validada automaticamente por testes **ArchUnit**
(`br.com.oficina.architecture.ArchitectureTest`).

```
br.com.oficina.<contexto>/
├── entities/        # Entidade de domínio + Value Objects — regra de negócio pura
├── usecases/        # Um Use Case por operação; fala com Gateways via interface
├── gateways/        # Ports (interfaces) — sem framework
├── presenters/      # Monta o Response/ViewModel a partir da saída do Use Case
├── controllers/     # Recebe DTO, chama Use Case, devolve via Presenter
└── infrastructure/  # Frameworks & Drivers: JPA (*Data), *GatewayImpl, config, mappers
```

**Fluxo (dependências apontam para dentro):**

```
Controller → Use Case → Entity → Use Case → Presenter → Controller
                 ↓
              Gateway (port) ← Gateway Impl (infra: JPA / e-mail / etc.)
```

> **Estado da migração:** os **6** bounded contexts — `ordemservico`, `servico`,
> `cliente`, `veiculo`, `peca` e `auth` — já estão 100% na estrutura Clean Architecture
> (`entities` / `usecases` / `gateways` / `presenters` / `controllers` / `infrastructure`).
> No `auth`, o acoplamento ao Spring Security foi isolado por ports (`TokenGateway`,
> `AutenticadorGateway`) para manter os Use Cases livres da camada de infraestrutura. A
> regra de dependência é validada em todos eles pelo teste `ArchitectureTest`.

---

## Pré-requisitos

- Java 21+
- Maven 3.9+ (ou usar o Maven Wrapper incluso: `./mvnw`)
- Docker e Docker Compose

---

## Rodando localmente

### 0. Entre no diretório do projeto

```bash
cd oficina-api
```

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

> O `settings.xml` na raiz do módulo `oficina-api` sobrepõe configurações de mirror corporativo do Maven. Use-o sempre com o wrapper local (por isso o `-s settings.xml` nos comandos acima).

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
| `WEBHOOK_ORCAMENTO_TOKEN` | *(valor de dev)* | Token compartilhado que autentica o webhook de decisão de orçamento |
| `NOTIFICACAO_EMAIL_ENABLED` | `false` | Habilita envio real de e-mail (produção). `false` = apenas log (dev/test) |
| `NOTIFICACAO_EMAIL_REMETENTE` | `nao-responder@oficina.com` | Remetente dos e-mails de status |
| `MAIL_HOST` / `MAIL_PORT` | `localhost` / `1025` | SMTP (Mailhog em dev; SendGrid/Gmail em produção) |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | *(vazio)* | Credenciais SMTP (produção) |

---

## Documentação da API

Com a aplicação rodando, acesse:

- **Swagger UI:** `http://localhost:8080/api/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/api/v3/api-docs`

---

## Autenticação

Todas as rotas administrativas exigem **JWT** (`Authorization: Bearer <token>`). As exceções (públicas) são:
`/auth/**`, `/public/**`, `/webhooks/**`, `/health`, `/actuator/**`, `/swagger-ui/**`, `/swagger-ui.html`, `/v3/api-docs/**`.
O endpoint de webhook é público para o JWT, mas autenticado por um token compartilhado no header `X-Webhook-Token`.

### Usuário admin padrão

Na primeira execução, um usuário `ADMIN` é criado automaticamente via `AdminInitializer`:

| Credencial | Valor padrão | Variável de ambiente |
|---|---|---|
| Username | `admin` | `ADMIN_USERNAME` |
| Password | `admin123` | `ADMIN_PASSWORD` |

> Em produção, **sempre** sobrescreva `ADMIN_USERNAME` / `ADMIN_PASSWORD` e `JWT_SECRET` via variáveis de ambiente.

### Obtendo um token

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

A resposta contém `token`. Use-o nas rotas protegidas:

```bash
curl http://localhost:8080/api/clientes \
  -H "Authorization: Bearer <token>"
```

---

## Endpoints principais

> Base path: `/api` · Documentação interativa: **Swagger UI**. A tabela abaixo é um resumo; consulte o Swagger para schemas e exemplos.

### Auth (público)
| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/auth/login` | Autenticar e retornar JWT |
| `POST` | `/api/auth/register` | Registrar novo usuário (role `MECANICO`) |

### Clientes
| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/clientes` | Cadastrar cliente (CPF ou CNPJ) |
| `GET` | `/api/clientes` | Listar clientes ativos |
| `GET` | `/api/clientes/{id}` | Buscar cliente por ID |
| `PUT` | `/api/clientes/{id}` | Atualizar cliente |
| `DELETE` | `/api/clientes/{id}` | Desativar cliente (soft delete) |

### Veículos
| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/veiculos` | Cadastrar veículo |
| `GET` | `/api/veiculos/cliente/{clienteId}` | Listar veículos de um cliente |
| `GET` | `/api/veiculos/{id}` | Buscar veículo por ID |
| `PUT` | `/api/veiculos/{id}` | Atualizar veículo |
| `DELETE` | `/api/veiculos/{id}` | Remover veículo |

### Serviços (catálogo)
| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/servicos` | Cadastrar serviço no catálogo |
| `GET` | `/api/servicos` | Listar serviços ativos |
| `GET` | `/api/servicos/{id}` | Buscar serviço por ID |
| `PUT` | `/api/servicos/{id}` | Atualizar serviço |
| `DELETE` | `/api/servicos/{id}` | Desativar serviço (soft delete) |

### Peças e Insumos
| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/pecas` | Cadastrar peça |
| `GET` | `/api/pecas` | Listar peças ativas |
| `GET` | `/api/pecas/{id}` | Buscar peça por ID |
| `PUT` | `/api/pecas/{id}` | Atualizar peça |
| `PATCH` | `/api/pecas/{id}/estoque` | Ajustar estoque (positivo entra, negativo sai) |
| `DELETE` | `/api/pecas/{id}` | Desativar peça (soft delete) |

### Ordens de Serviço
| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/ordens-servico` | Criar OS (vincula cliente e veículo) |
| `POST` | `/api/ordens-servico/abertura` | **Abertura consolidada**: cliente + veículo + serviços + peças numa única chamada; retorna o ID da OS |
| `GET` | `/api/ordens-servico` | Listar OSs — **ordenadas por prioridade de status** (Em Execução > Aguardando Aprovação > Diagnóstico > Recebida), mais antigas primeiro; **exclui** FINALIZADA/ENTREGUE/CANCELADA. Filtros por `clienteId`/`status` |
| `GET` | `/api/ordens-servico/{id}` | Buscar OS por ID |
| `POST` | `/api/ordens-servico/{id}/servicos` | Adicionar item de serviço à OS |
| `DELETE` | `/api/ordens-servico/{id}/servicos/{itemServicoId}` | Remover item de serviço |
| `POST` | `/api/ordens-servico/{id}/pecas` | Adicionar peça (reserva estoque) |
| `DELETE` | `/api/ordens-servico/{id}/pecas/{itemPecaId}` | Remover peça (devolve estoque) |
| `PATCH` | `/api/ordens-servico/{id}/status` | Avançar status (máquina de estados) |
| `POST` | `/api/ordens-servico/{id}/aprovar-orcamento` | Aprovar orçamento (→ `EM_EXECUCAO`) |
| `POST` | `/api/ordens-servico/{id}/recusar-orcamento` | Recusar orçamento (→ `CANCELADA`) |
| `GET` | `/api/ordens-servico/monitoramento/tempo-medio-execucao` | Tempo médio de execução (min) |

### Consulta pública (para o cliente final)
| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/public/ordens-servico/{id}/status` | Consultar status da OS sem autenticação |

### Webhook (integração externa)
| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/webhooks/orcamento` | Recebe a decisão de orçamento (`APROVADO`/`RECUSADO`) de um sistema externo. Autenticado pelo header `X-Webhook-Token`; idempotente. `APROVADO` → `EM_EXECUCAO`, `RECUSADO` → `CANCELADA` |

### Operacional
| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/health` | Status simples da aplicação |
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

## Notificações por e-mail

As transições relevantes da OS (`AGUARDANDO_APROVACAO`, `EM_EXECUCAO`, `FINALIZADA`,
`ENTREGUE`, `CANCELADA`) disparam um e-mail ao cliente. A arquitetura mantém o port
`NotificacaoGateway` no núcleo e dois adapters na infra, selecionados por
`NOTIFICACAO_EMAIL_ENABLED`:

- **`false` (dev/test):** `LogNotificacaoGateway` — apenas registra em log, sem envio real.
- **`true` (produção/compose/k8s):** `EmailNotificacaoGateway` — envia via SMTP (`spring.mail.*`).

Em desenvolvimento, o `docker compose` sobe um **Mailhog** (UI em `http://localhost:8025`)
que captura os e-mails sem enviá-los de verdade. Falhas de envio são registradas e
**não quebram** a transação de negócio.

---

## Deploy em Kubernetes (kind)

Manifestos em [`/k8s`](k8s) (Deployment, Service NodePort, ConfigMap, Secret, HPA,
PostgreSQL `StatefulSet`+`PVC`, Mailhog). Ver [`k8s/README.md`](k8s/README.md) para o
passo a passo completo. Resumo:

```bash
# 1. cluster + metrics-server (ver k8s/README.md)
kind create cluster --name oficina --config infra/kind-config.yaml
# 2. imagem
docker build -t oficina-api:latest oficina-api
kind load docker-image oficina-api:latest --name oficina
# 3. deploy
kubectl apply -k k8s
kubectl -n oficina rollout status deploy/oficina-api
```

- API: `http://localhost:30080/api/swagger-ui.html` · Mailhog: `http://localhost:30825`

### Escalabilidade (HPA)

O `HorizontalPodAutoscaler` escala a app de **2 a 6 réplicas** por CPU (60%) e memória (75%).
Requer `metrics-server`. Para demonstrar:

```bash
kubectl get hpa,pods -n oficina -w        # observar
./k8s/load-test.sh http://localhost:30080 60s 50   # gerar carga
```

---

## Provisionamento com Terraform

[`/infra`](infra) provisiona o cluster kind, o metrics-server e aplica os manifestos
(app + banco) via Terraform. Ver [`infra/README.md`](infra/README.md).

```bash
docker build -t oficina-api:latest oficina-api   # imagem local
cd infra
terraform init
terraform apply -auto-approve
# ...
terraform destroy -auto-approve                  # limpa tudo
```

---

## CI/CD (GitHub Actions)

| Workflow | Função |
|---|---|
| [`ci.yml`](.github/workflows/ci.yml) | Build + testes (JUnit / Testcontainers / ArchUnit) + relatório JaCoCo via `mvn verify`, publicado como artifact. Dispara em push na `main` e em PRs |
| [`cd.yml`](.github/workflows/cd.yml) | Build da imagem Docker → push no **GHCR** → deploy no cluster (quando há credenciais) |
| [`claude-review.yml`](.github/workflows/claude-review.yml) | Revisão automática de PRs |

> A análise de vulnerabilidades **OWASP Dependency-Check** está configurada no `pom.xml`
> (`failBuildOnCVSS=9`), mas roda **sob demanda** — não faz parte do `mvn verify` do CI:
> ```bash
> ./mvnw -s settings.xml org.owasp:dependency-check-maven:check
> ```
> O relatório mais recente está versionado em
> [`vulnerability-report/dependency-check-report.html`](vulnerability-report/dependency-check-report.html).

**Secrets do pipeline** (Settings → Secrets and variables → Actions):

| Secret | Uso | Obrigatório |
|---|---|---|
| `GITHUB_TOKEN` | Push da imagem no GHCR (fornecido automaticamente) | — |
| `KUBECONFIG_B64` | Kubeconfig do cluster (base64) para o job de deploy | Opcional* |

\* Sem `KUBECONFIG_B64`, o job de deploy é **pulado com aviso** (não falha o pipeline) —
a imagem é publicada normalmente no GHCR. Gere com:
`base64 -w0 ~/.kube/config` e cole no secret.

---

## Vídeo demonstrativo

> 🎥 **Link do vídeo:** _(a ser adicionado)_ — demonstra deploy da aplicação,
> execução do CI/CD, consumo das APIs e escalabilidade automática (HPA sob carga).

---

## Coleção de APIs (Postman / Swagger)

A especificação **OpenAPI** é a fonte da coleção e pode ser importada direto no
Postman/Insomnia (*Import → Link*):

- Swagger UI: `http://localhost:8080/api/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api/v3/api-docs`

---

## Justificativa do banco de dados

**PostgreSQL** foi escolhido por:
- Suporte robusto a UUID nativo (tipo `uuid`)
- Confiabilidade e maturidade em produção
- Suporte completo a transações ACID
- Compatibilidade com Flyway e Hibernate/JPA
- Open source com ampla adoção na indústria

---

## Documentação complementar

| Documento | Conteúdo |
|---|---|
| [`k8s/README.md`](k8s/README.md) | Deploy em Kubernetes (kind): manifestos, cluster, HPA, autoscaling |
| [`infra/README.md`](infra/README.md) | Provisionamento do ambiente via Terraform (kind + metrics-server + deploy) |
| [`vulnerability-report/dependency-check-report.html`](vulnerability-report/dependency-check-report.html) | Relatório OWASP Dependency-Check gerado |
| [Board Miro — Documentação DDD](https://miro.com/app/board/uXjVOXA0ID4=/) | Event Storming, Context Map, Agregados e Linguagem Ubíqua |