# CLAUDE.md — delivery-system (Java Advanced · Projeto Diamante 02)

Sistema de pedidos de delivery com microsserviços. Monorepo Gradle com 4 projetos, que precisa cumprir **exatamente** o contrato da spec, porque o app React Native do professor (github.com/joaocarloslima/diamante-delivery) consome a API sem ajustes.

**Equipe de 2 integrantes: Gabriel e Orlando.** O trabalho é dividido por fatias (ver "Divisão de trabalho" na seção 1) e **todas as regras de aprovação deste arquivo valem igualmente para os dois**.

> **A spec é a fonte da verdade.** Em caso de dúvida entre "o que seria melhor" e "o que a spec diz", vale a spec. A spec em PDF está em `docs/spec/`.

---

## 1. Metodologia: Spec-Driven Development (SDD)

Nenhum código é escrito antes de existir spec, plano e tasks da fatia correspondente.

### Fluxo obrigatório por fatia (feature slice)

1. **Specify** — escrever/atualizar `specs/<NN>-<slice>/spec.md` com: objetivo, comportamento esperado, contrato (rotas, JSON, status HTTP) e **critérios de aceitação verificáveis** extraídos da spec do professor.
2. **Plan** — `specs/<NN>-<slice>/plan.md`: classes/beans/configs, decisões técnicas, riscos.
3. **Tasks** — `specs/<NN>-<slice>/tasks.md`: checklist pequeno e ordenado (cada task cabe em um commit).
4. **Implement** — executar as tasks em ordem, marcando `[x]`. Não implementar nada fora das tasks; se surgir algo novo, voltar ao passo 1/2/3 antes.
5. **Verify** — rodar os critérios de aceitação (testes automatizados e/ou `curl`/script). Só então a fatia é considerada pronta.

### Regras SDD
- Se o código divergir da spec, **o código está errado** (ou a spec do repo precisa ser corrigida de forma explícita e justificada, nunca silenciosamente).
- Ambiguidade na spec do professor → registrar em `specs/OPEN-QUESTIONS.md` e perguntar ao dono da sessão; não decidir calado.
- Commits pequenos, um por task ou grupo coeso, mensagem em inglês no formato `type(scope): message` (ex.: `feat(order): reserve stock with pessimistic lock`).

### Pontos de aprovação (OBRIGATÓRIO parar e esperar o "ok" do dono da sessão)

O Claude **nunca avança sozinho** entre etapas. Paradas obrigatórias:

1. **Depois de escrever `spec.md`, `plan.md` e `tasks.md` de uma fatia** → mostrar um resumo curto e **parar**. Não implementar nada até o dono da sessão aprovar.
2. **Depois de cada task concluída** → marcar `[x]` em `tasks.md`, dizer em 2–3 linhas o que foi feito (arquivos criados/alterados e como verificar) e **parar**. Só iniciar a próxima task quando o dono da sessão disser "ok", "pode seguir" ou equivalente.
3. **Depois de concluir a verificação de uma fatia** → reportar o resultado dos critérios de aceitação e **parar**. Não criar a spec da fatia seguinte sem pedido explícito.
4. **Qualquer ambiguidade, divergência da spec ou decisão não prevista** → parar e perguntar, sem escolher por conta própria.

Um "ok" vale **só para a etapa que acabou de ser mostrada**, não para as próximas. Não encadear várias tasks em uma única resposta, mesmo que pareçam pequenas.

### Ordem das fatias (cada uma vira uma pasta em `specs/`)

| # | Fatia | Pontos |
|---|-------|--------|
| 00 | Infra: monorepo Gradle, `docker-compose.yml`, README | 5 (Docker e contrato) |
| 01 | `eureka-server` + registro dos serviços | 10 |
| 02 | `payment-service` (2 instâncias, falha ~50%) | 15 (load balance) |
| 03 | `order-service`: Dish, DataLoader, GET /dishes | — |
| 04 | `order-service`: POST /orders com lock pessimista + transação | 15 (race condition) |
| 05 | `order-service`: PaymentClient com `@LoadBalanced` + `@Retryable` + 502 | 15 (retry) |
| 06 | Producer: POST /reviews → RabbitMQ | 15 (mensageria) |
| 07 | `review-service`: consumer + buffer + flush + ranking | 15 (backpressure) |
| 08 | `order-service`: POST /assistant com Spring AI | 10 |
| 09 | Bônus: rate limit em POST /orders | +10 (teto 100) |
| 10 | Verificação final contra a checklist da seção 9 | — |

### Divisão de trabalho (equipe de 2)

Dividida por **nível de dificuldade** (nota de 1 a 5 por fatia) para ficar justa. A coluna "Dono" indica quem assume cada lado.

| Lado | Dono | Fatias | Dificuldade |
|------|------|--------|-------------|
| **A** | **Gabriel** | 00, 01, 02, 03, 04, 05 — infra, Eureka, payment, núcleo do order-service (Dish, pedidos com lock, retry/502) | 15 |
| **B** | **Orlando** | 06, 07, 08, 09 — producer de reviews, review-service, assistente Spring AI, bônus de rate limit | 13 |
| **Ambos** | — | 10 (verificação final) + README com nome e RM dos dois | — |

As fatias 04 e 05 ficam com a mesma pessoa de propósito: dependem da mesma decisão de design (pagamento dentro da transação, lock preso durante os retries; ver seção 5).

**Quem é o "dono da sessão".** É a pessoa que está conversando com o Claude agora. **Gabriel = lado A. Orlando = lado B.** No início de cada sessão, se a pessoa não tiver dito quem é, o Claude **pergunta**: "Você é o Gabriel ou o Orlando?". Ao saber quem é, ele já assume o lado correspondente: lista as fatias da pessoa, diz qual é a próxima a fazer (respeitando as dependências abaixo) e **espera o ok** antes de começar. Daí em diante:
- O Claude trabalha **só nas fatias do lado do dono da sessão**. Se a tarefa exigir mexer em fatia do outro lado, **parar e perguntar**; não editar.
- O "ok" que libera cada etapa só vale se vier **do dono da sessão**, para a etapa que acabou de ser mostrada. Um ok dado pelo outro integrante em outra sessão não vale.
- As regras de parada (pontos de aprovação), sugestões de melhoria com trade-offs e perguntas em caso de ambiguidade são **as mesmas para os dois lados**.

**Dependências entre os lados (ordem sugerida).**
- A fatia **00** (scaffold Gradle + compose) é pré-requisito de todas: o lado A faz primeiro e entra na `main` antes de o lado B começar.
- O lado B pode começar pela **07** (review-service) logo após a 00, porque só depende do RabbitMQ. As fatias **06 e 08** precisam da **03** (Dish) já na `main`.
- A fatia **05** altera o fluxo de pedidos criado na **04**; ambas são do lado A, então não há conflito entre pessoas.
- A fatia **09** (rate limit) só começa depois de **04 e 05** na `main`, pois adiciona filtro sobre `POST /orders`.

**Fluxo de Git para a dupla.**
- Uma branch por fatia: `feat/<NN>-<slice>` (ex.: `feat/04-orders-lock`), saindo de uma `main` atualizada (`git pull` antes de começar).
- Cada fatia entra na `main` por **Pull Request**, revisado pelo outro integrante, depois da verificação da fatia (ponto de aprovação 3).
- Arquivos **compartilhados** (`settings.gradle(.kts)`, `build.gradle(.kts)` raiz, `docker-compose.yml`, `README.md`, `CLAUDE.md`, `specs/OPEN-QUESTIONS.md`): alterar só quando necessário, em commit pequeno e avisando o outro integrante, para evitar conflito de merge. `OPEN-QUESTIONS.md` pode receber entradas dos dois (apenas acrescentar).
- Cada fatia tem sua pasta em `specs/<NN>-<slice>/`; ela pertence ao dono da fatia.
- Mudança no **contrato** (seção 3) ou nas **decisões de design** (seção 5) afeta os dois lados: o Claude **não altera sozinho**; precisa de ok explícito dos dois integrantes.

---

## 2. Stack e restrições globais

- **Java 25**, **Spring Boot 4**, **Spring Cloud 2025.1.x**, **Gradle** (multi-project).
- Monorepo `delivery-system` com exatamente 4 projetos: `eureka-server`, `order-service`, `payment-service`, `review-service`.
- Banco: **H2** em memória (a spec cita H2 no review-service; usar H2 também no order-service).
- RabbitMQ via Docker: `rabbitmq:4-management`, portas **5672** e **15672**, no `docker-compose.yml` na raiz.
- **Todo o código em inglês**: entidades, campos, endpoints, filas, mensagens de erro e logs. (A única exceção é a resposta do assistente de IA, que é em português.)
- **Nenhuma URL com `localhost` entre serviços.** Toda chamada usa o nome registrado no Eureka (ex.: `http://PAYMENT-SERVICE/payments`). → **−10 pontos** se violar.
- **Chave de API do modelo de IA nunca vai para o repositório**: usar variável de ambiente (`${OPENAI_API_KEY}`), nunca valor literal em `application.yml`/`.properties`. → **−10 pontos** se violar.
  - No `application.yml`, a propriedade do Spring AI recebe só o placeholder, **sem valor padrão real** (ex.: `api-key: ${OPENAI_API_KEY}`; confirmar o nome exato da propriedade na documentação da versão usada). Se usar modelo local (LM Studio/Ollama), a `base-url` também vem de variável de ambiente (ex.: `${AI_BASE_URL}`), sem `localhost` hardcoded.
  - O repositório tem um `.env.example` com **só os nomes** das variáveis (valores vazios). O `.env` real e qualquer `application-local.*` ficam no `.gitignore`.
  - O Claude **nunca** pede a chave no chat, **nunca** imprime o valor, **nunca** lê o `.env` e **nunca** escreve a chave em arquivo versionado, log, teste ou documentação. Se precisar da chave para rodar, orienta o integrante a definir a variável no próprio terminal/IDE.
  - Se uma chave vazar para o Git, revogar/gerar outra na hora; apagar o commit não basta.
- Entrega fora do formato exigido → **−10 pontos** (ver seção 8).

### Portas

| Serviço | Porta |
|---------|-------|
| order-service | 8080 |
| payment-service (instância 1) | 8081 |
| payment-service (instância 2) | 8082 |
| review-service | 8083 |
| eureka-server | 8761 |
| RabbitMQ AMQP / UI | 5672 / 15672 |

O app React Native usa **apenas :8080 e :8083**.

### `spring.application.name`
Os três serviços de negócio se registram no Eureka pelo `spring.application.name`: `order-service`, `payment-service`, `review-service`. O Eureka resolve o nome em maiúsculas (`PAYMENT-SERVICE`).

---

## 3. Contrato público (NÃO ALTERAR)

Rotas, nomes de campos e status são obrigatórios.

### order-service :8080

| Método | Rota | Corpo | Respostas |
|--------|------|-------|-----------|
| GET | `/dishes` | — | 200 lista de Dish |
| GET | `/dishes/{id}` | — | 200 Dish · 404 |
| POST | `/orders` | `{"dishId": 1, "quantity": 2}` | 201 Order · 400 · 404 · 409 · 502 |
| GET | `/orders/{id}` | — | 200 Order · 404 |
| POST | `/reviews` | `{"dishId": 1, "rating": 5, "comment": "Great"}` | 202 sem corpo · 400 · 404 |
| POST | `/assistant` | `{"question": "Tem opção vegana?"}` | 200 `{"answer": "..."}` |

### review-service :8083
| Método | Rota | Resposta |
|--------|------|----------|
| GET | `/reviews/ranking` | 200 lista ordenada pela média (desc) |

### payment-service :8081 / :8082 (interno, o app não chama)
| Método | Rota | Corpo | Respostas |
|--------|------|-------|-----------|
| POST | `/payments` | `{"amount": 79.80}` | 200 `{"status": "APPROVED", "instance": 8081}` · 500 falha simulada |

### Formatos JSON

```json
// Dish
{ "id": 1, "name": "House Burger", "description": "Brioche bun", "price": 39.90, "stock": 10 }

// Order
{ "id": 7, "dishId": 1, "quantity": 2, "totalPrice": 79.80, "status": "CONFIRMED", "createdAt": "2026-10-08T20:15:00" }

// Erro (4xx e 502) — sempre este formato, mensagem em inglês
{ "error": "Dish out of stock" }

// GET /reviews/ranking
[{ "dishId": 1, "dishName": "House Burger", "average": 4.6, "count": 128 }]
```

- Status do pedido: **`CONFIRMED`**.
- Datas em ISO-8601 (`LocalDateTime` sem offset, sem timestamp numérico). Valores monetários como número com ponto decimal (`BigDecimal`).
- Todo erro 4xx/502 do order-service retorna `{"error": "..."}`. Centralizar em um `@RestControllerAdvice`.
- Mapa de erros do `POST /orders`: quantity < 1 → **400** · prato inexistente → **404** · sem estoque → **409** · pagamento falhou após todas as tentativas → **502**.
- `POST /reviews`: rating fora de 1–5 → **400** · prato inexistente → **404** · sucesso → **202 sem corpo**.

---

## 4. Requisitos por serviço

### 4.1 eureka-server (:8761)
- `@EnableEurekaServer`. Não se registra em si mesmo (`register-with-eureka: false`, `fetch-registry: false`).
- Os 3 serviços aparecem registrados (payment com 2 instâncias).

### 4.2 payment-service (:8081 e :8082)
- `POST /payments` retorna **500 em ~50%** das chamadas (usar `java.util.Random`).
- Duas instâncias, ambas registradas no Eureka. A segunda sobe com `./gradlew :payment-service:bootRun --args='--server.port=8082'`.
- Resposta de sucesso e **log** informam a porta da instância que atendeu (`"instance": <port>`). A alternância entre portas precisa ser visível nos logs.
- Como as duas instâncias compartilham o mesmo `spring.application.name`, configurar `eureka.instance.instance-id` com a porta (ex.: `${spring.application.name}:${server.port}`) para que ambas apareçam separadas no Eureka.

### 4.3 order-service (:8080)

**Pedidos e estoque**
- Entidade `Dish`: `id`, `name`, `description`, `price` (`BigDecimal`), `stock` (`int`).
- `DataLoader` com **pelo menos 5 pratos**; o prato da promoção começa com **stock 10**.
- Entidade `CustomerOrder`: `id`, `dishId`, `quantity`, `totalPrice`, `status`, `createdAt` (`LocalDateTime`). (O nome `CustomerOrder` evita a palavra reservada `ORDER` do SQL; o JSON continua sendo `Order` conforme o contrato.)
- `POST /orders` reserva o estoque com **`@Lock(LockModeType.PESSIMISTIC_WRITE)`** (query no repository) + **`@Transactional`**.
- Critério de aceitação: 50 requisições simultâneas no prato da promoção → **exatamente 10 pedidos confirmados** e `stock` termina em **0**.

**Load balance e retry**
- `RestTemplate` com `@LoadBalanced` chamando `http://PAYMENT-SERVICE/payments`.
- A chamada fica em um **bean separado** (`PaymentClient`) com **`@Retryable`** e **`@EnableResilientMethods`** (resiliência nativa do Spring Framework 7, não o spring-retry antigo).
- Configurar `maxRetries`, `delay`, `multiplier` (backoff exponencial), `jitter` e `maxDelay`. Confirmar os nomes/unidades exatos dos atributos na documentação do Spring Framework 7 antes de implementar.
- `@Retryable` só funciona via proxy: a chamada **tem que vir de outro bean** (nunca de `this.`).
- Se todas as tentativas falharem: pedido **não confirmado**, **estoque intacto**, resposta **502**.

**Mensageria (producer)**
- `POST /reviews` valida rating (1–5), publica a avaliação e responde **202** sem gravar no banco.
- Mensagem JSON: `dishId`, `dishName`, `rating`, `comment` (por isso o order-service busca o prato para obter `dishName`; prato inexistente → 404).
- Configuração **explícita** (beans): `TopicExchange` **`delivery.exchange`**, `Queue` durável **`reviews.queue`**, routing key **`reviews.new`** e o `Binding`.

**Assistente (Spring AI)**
- `POST /assistant` recebe `question`, devolve `answer`.
- `ChatService` com `ChatClient` criado a partir do `ChatClient.Builder`.
- **System message**: atendente do restaurante, respostas curtas em português.
- O cardápio atual (`name`, `price`, `stock`) é lido **do banco a cada pergunta** e entra no prompt.
- Perguntas fora do tema do restaurante são recusadas com educação ("Quem ganhou a Copa de 2002?" → recusa e volta ao assunto).
- Provedor à escolha: OpenAI ou modelo local (LM Studio / Ollama via `base-url`). Chave sempre por variável de ambiente.

### 4.4 review-service (:8083)
- `@RabbitListener` em `reviews.queue` acumula as avaliações em um **`ConcurrentHashMap`** por prato (soma dos ratings + quantidade).
- **`@Scheduled` a cada 5 s** grava o acumulado no H2 (entidade `ReviewSummary`) e **limpa o buffer** (`@EnableScheduling`).
- O flush deve **somar** ao `ReviewSummary` já existente (upsert acumulativo), nunca sobrescrever. Trocar a entrada do buffer de forma atômica (ex.: `compute`/`remove` por chave) para não perder avaliações que chegam durante o flush.
- `GET /reviews/ranking` **lê do banco** e devolve os pratos ordenados pela média (desc).

### 4.5 Bônus: rate limit (+10, nota máxima continua 100)
- Limitar `POST /orders` a no máximo **20 req/s**; excedente → **429** com `{"error": "..."}`.
- Implementação livre (Bucket4j, Resilience4j ou própria).
- ⚠️ Tensão com o teste de 50 requisições simultâneas: o limite faz parte das 50 receberem 429. Implementar **por último**, com o limite **configurável por propriedade**, e validar que o critério de race condition continua coerente.

---

## 5. Decisões de design (registrar e manter)

1. **Pagamento dentro da transação do pedido.** Fluxo de `POST /orders`: abrir transação → `SELECT ... FOR UPDATE` no prato → validar estoque (409) → decrementar → chamar `PaymentClient` → se OK, salvar pedido `CONFIRMED`; se falhar após os retries, lançar exceção que **faz rollback** (estoque intacto) e vira 502.
2. **Cuidado com o lock durante os retries.** Com lock pessimista, requisições ficam serializadas e cada uma segura o lock durante o retry do pagamento. Manter `delay`/`maxDelay` curtos (centenas de ms) e `maxRetries` suficiente para que, com 50% de falha e 2 instâncias, os 10 pedidos do teste passem de forma confiável. Se `maxRetries` for baixo demais, alguns pedidos legítimos virarão 502 e o teste "exatamente 10 confirmados" falhará. Ficar de olho também no pool do Hikari e no timeout do lock com 50 requisições em paralelo.
3. **Mensagem de avaliação**: serialização JSON via converter do Spring AMQP (seguir o padrão do exemplo do professor).
4. **Validação**: Bean Validation nos DTOs de entrada + tratamento centralizado no `@RestControllerAdvice` convertendo tudo para `{"error": "..."}`.

---

## 6. Referências do professor

Os exemplos do professor estão em (atenção às maiúsculas nos nomes das pastas):

```
docs/references/springAI/     # exemplo de Spring AI (ChatClient/ChatService)
docs/references/rabbitMQ/     # exemplo de RabbitMQ (producer, consumer e config)
```

### Como usar as referências
- São **apenas guias de estilo e organização**: servem para ver como o professor estrutura o código (pacotes, nomes de classes e beans, estilo de configuração, padrões de DTO/config). **Não copiar o código cegamente**: sempre **adaptar ao contexto do nosso projeto** (nomes em inglês do contrato, entidades `Dish`/`CustomerOrder`/`ReviewSummary`, exchange/fila/routing key definidos na spec, versões Java 25 / Boot 4).
- Antes de implementar as fatias 06, 07 e 08, **ler a referência correspondente** e seguir o mesmo estilo.
- Se a referência divergir da spec, a **spec vence**; avisar o dono da sessão.

### Sugestões de melhoria (REGRA FIXA)
Se o Claude identificar uma forma melhor de fazer algo (em relação à referência do professor, à spec ou ao que já foi planejado), ele **não implementa por conta própria**. Deve:
1. **Parar** e descrever a sugestão.
2. **Mostrar os trade-offs** (ganhos, custos, riscos, impacto na nota e no contrato) comparando com a abordagem original.
3. **Esperar o ok explícito do dono da sessão** antes de implementar. Sem ok, vale a abordagem original.

### Regra geral
**Antes de implementar qualquer coisa, esperar o ok do dono da sessão. Sempre.**

---

## 7. Estrutura esperada do repositório

```
delivery-system/
├── CLAUDE.md
├── README.md                 # nome e RM de cada integrante (obrigatório)
├── docker-compose.yml        # RabbitMQ
├── settings.gradle(.kts)     # include das 4 pastas
├── build.gradle(.kts)        # config comum (opcional)
├── docs/
│   ├── spec/                 # PDF da spec do projeto
│   └── references/{springAI,rabbitMQ}/   # exemplos do professor (só referência de estilo)
├── specs/                    # começa vazia; o Claude gera aqui as specs de cada fatia
│   ├── OPEN-QUESTIONS.md
│   └── <NN>-<slice>/{spec.md,plan.md,tasks.md}
├── eureka-server/
├── order-service/
├── payment-service/
└── review-service/
```

**`.gitignore` (criado na fatia 00).** Deve incluir, no mínimo: `docs/references/`, `docs/spec/`, `.env`, `application-local.*`, `build/`, `.gradle/`, `.idea/`, `*.iml`, `*.log` e arquivos locais do H2 (`*.mv.db`, `*.trace.db`). As pastas `docs/references/` e `docs/spec/` **não são versionadas** (material do professor): cada integrante recebe por fora e coloca nos mesmos caminhos. Se elas não existirem na máquina, o Claude pede ao dono da sessão em vez de inventar o conteúdo. `specs/`, `CLAUDE.md` e `README.md` **são versionados** (fazem parte do portfólio).

Convenção de pacotes por serviço (ex.: `br.com.fiap.delivery.order`): `controller`, `service`, `repository`, `model` (ou `entity`), `dto`, `config`, `client`, `exception`.

---

## 8. Como rodar e entregar

```bash
docker compose up -d                                             # RabbitMQ
./gradlew :eureka-server:bootRun
./gradlew :payment-service:bootRun                               # :8081
./gradlew :payment-service:bootRun --args='--server.port=8082'   # :8082
./gradlew :review-service:bootRun                                # :8083
./gradlew :order-service:bootRun                                 # :8080
```

Ordem de subida: Eureka → payment (2x) → review → order. Variável do assistente: `export OPENAI_API_KEY=...` (ou apontar `base-url` para LM Studio/Ollama).

**Entrega (fora do formato = −10):**
- Enviar o link do repositório GitHub no site **geminidev**.
- Repositório com o monorepo `delivery-system`, público ou com acesso liberado ao professor.
- README com nome e RM de cada integrante.
- O projeto sobe com `docker compose up` + os quatro serviços via `bootRun`.

---

## 9. Checklist de verificação final (espelha a avaliação)

| Critério | O que será verificado | Pts | OK |
|----------|----------------------|-----|----|
| Eureka | eureka-server no ar e os 3 serviços registrados | 10 | [ ] |
| Load balance | 2 instâncias do payment-service, chamada pelo nome, alternância visível nos logs | 15 | [ ] |
| Retry | `@Retryable` em bean separado, backoff exponencial e jitter; 502 com estoque intacto | 15 | [ ] |
| Race condition | lock pessimista + transação: 50 requisições simultâneas → exatamente 10 pedidos | 15 | [ ] |
| Mensageria | exchange, fila durável, routing key e binding explícitos; POST /reviews responde 202 | 15 | [ ] |
| Backpressure | buffer concorrente, flush agendado, ranking lido do banco | 15 | [ ] |
| Spring AI | ChatClient, system message, cardápio no prompt, recusa fora do tema | 10 | [ ] |
| Docker e contrato | compose sobe o RabbitMQ e o app React Native funciona sem ajustes | 5 | [ ] |
| Bônus | rate limit 20 req/s em POST /orders (429) | +10 | [ ] |

Descontos a evitar: `localhost` entre serviços (−10) · chave de API no repo (−10) · entrega em formato incorreto (−10).

Verificações práticas:
- `grep -rn "localhost" --include=*.java --include=*.yml --include=*.properties .` → nada entre serviços (exceto URL do próprio Eureka, se necessário, e RabbitMQ local).
- `git grep -nE "sk-|api[-_]?key\s*[:=]\s*['\"]?[A-Za-z0-9]{10,}"` → nenhuma chave.
- Script de carga (50 `curl` em paralelo ou teste com `ExecutorService`) em `POST /orders` no prato da promoção → contar 201 == 10 e `GET /dishes/{id}` com `stock == 0`.
- Disparar muitos `POST /reviews` e conferir no RabbitMQ UI (:15672) que a fila é consumida e o ranking só muda após o flush de 5 s.
- Testar o app React Native contra :8080 e :8083 sem alterar nada nele.

---

## 10. Instruções para o Claude neste repositório

- Siga o fluxo SDD da seção 1 e respeite os **pontos de aprovação**: uma task por vez, parando a cada uma até o dono da sessão dar o ok. Pergunte antes de decidir qualquer ambiguidade da spec.
- **Nunca implemente nada sem o ok explícito do dono da sessão.** Se tiver uma sugestão de melhoria, pare, explique e mostre os trade-offs (seção 6) antes de qualquer mudança.
- O projeto tem **2 integrantes (Gabriel e Orlando)**: confirme quem é o dono da sessão, trabalhe só nas fatias do lado dele e só aceite o ok de quem está na sessão (ver "Divisão de trabalho", seção 1).
- Use `docs/references/` apenas como guia de estilo/organização; adapte ao nosso contexto, não copie.
- Nunca altere rotas, nomes de campos, status HTTP ou formatos JSON da seção 3.
- Nunca escreva `localhost` em chamadas entre serviços nem segredo em arquivo versionado.
- Código, comentários, logs e mensagens de erro em **inglês**; explicações para o dono da sessão em **português**.
- Prefira solução simples que cumpra a spec; não adicione bibliotecas, camadas ou features não pedidas (o projeto estima ~4 h de trabalho).
- Ao finalizar uma fatia, rode a verificação dela e marque a task como concluída em `tasks.md`.
- Spring Boot 4 / Spring Framework 7 / Spring AI mudaram APIs em relação às versões anteriores: confirme a API na documentação oficial em vez de assumir de memória.