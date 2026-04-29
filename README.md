# 📅 Task Scheduler AI

**Um gerenciador de tarefas inteligente com autenticação JWT, sugestões de IA e notificações por email.**

> Organize suas tarefas usando linguagem natural. A IA extrai automaticamente datas, horas e prioridades em português. Receba lembretes por email 1 hora antes de cada vencimento.

---

## 🎯 Visão Geral do Projeto

Task Scheduler AI é uma aplicação full-stack moderna que combina:
- **Autenticação JWT segura** com senhas criptografadas (BCrypt)
- **IA integrada** para processar prompts em linguagem natural
- **Extração automática de datas em português** ("amanhã", "próxima segunda", "5 de maio às 14h")
- **Notificações por email** em HTML com design profissional
- **API REST** com validação e tratamento de erros
- **Frontend moderno** com React + Vite + Tailwind CSS

---

## 🔐 Publicação Segura no GitHub

Antes de subir este projeto:

```bash
cp .env.example .env
```

Preencha o `.env` com valores reais e mantenha-o fora do Git. Este repositório já ignora:

- `.env` e variantes locais
- logs
- artefactos de build (`backend/target`, `frontend/dist`, `node_modules`)
- dados locais do Docker

Checklist mínima antes do push:

1. Confirmar que `.env` não está staged.
2. Usar um `JWT_SECRET` forte e único.
3. Trocar passwords padrão de base de dados.
4. Não publicar chaves de email ou API reais em commits, screenshots ou README.

---

## 🐳 Docker Compose Seguro

O projeto inclui um `docker-compose.yml` para subir a aplicação completa:

- `postgres` fica apenas na rede interna do Compose
- `backend` expõe a API em `BACKEND_PORT`
- `frontend` expõe a UI em `FRONTEND_PORT`
- segredos obrigatórios vêm do `.env`, sem fallback inseguro para JWT

### 1. Preparar ambiente

```bash
cp .env.example .env
```

Edite o `.env` e ajuste pelo menos:

```bash
DB_PASSWORD=troque-esta-password
JWT_SECRET=um-segredo-longo-e-aleatorio
```

### 2. Subir tudo

```bash
docker compose up --build -d
```

### 3. Acessos

```bash
Frontend: http://localhost:5173
Backend:  http://localhost:8080
Swagger:  http://localhost:8080/swagger-ui.html
```

### 4. Parar stack

```bash
docker compose down
```

---

## 🏗️ Arquitetura

### Diagrama de Camadas

```
┌─────────────────────────────────────────────────────────┐
│                    FRONTEND (React)                      │
│              http://localhost:5173                       │
│  LoginPage → Dashboard (TaskList + AIChat)              │
└──────────────────────┬──────────────────────────────────┘
                       │ HTTP/REST + CORS
┌──────────────────────▼──────────────────────────────────┐
│                  API Gateway (Vite Proxy)               │
│              /api → http://localhost:8080               │
└──────────────────────┬──────────────────────────────────┘
                       │ REST/JSON
┌──────────────────────▼──────────────────────────────────┐
│          BACKEND (Spring Boot 3.2.4)                     │
│              http://localhost:8080                       │
│                                                          │
│  ┌────────────────────────────────────────────────────┐ │
│  │  Controllers (Auth, Task, Conversation, User)      │ │
│  │  - POST /api/auth/login, /register                 │ │
│  │  - POST /api/tasks/from-prompt (com IA)            │ │
│  └────────────────────────────────────────────────────┘ │
│                         ▲                                │
│                    Spring DI                             │
│  ┌────────────────────────────────────────────────────┐ │
│  │  Services                                          │ │
│  │  - TaskService (CRUD + email)                      │ │
│  │  - ClaudeService (IA + date extraction)            │ │
│  │  - EmailService (SMTP HTML)                        │ │
│  │  - ReminderService (Quartz jobs)                   │ │
│  │  - SecurityConfig + JwtUtil                        │ │
│  └────────────────────────────────────────────────────┘ │
│                         ▲                                │
│                    Spring DI                             │
│  ┌────────────────────────────────────────────────────┐ │
│  │  Repositories (Spring Data JPA)                    │ │
│  │  - TaskRepository, UserRepository, etc             │ │
│  └────────────────────────────────────────────────────┘ │
└──────────────────────┬──────────────────────────────────┘
                       │ JDBC
┌──────────────────────▼──────────────────────────────────┐
│    DATABASE (PostgreSQL 15)                              │
│    - users (autenticação)                               │
│    - tasks (tarefas do usuário)                         │
│    - ai_conversations (histórico de IA)                 │
│    - reminder_logs (registro de lembretes)              │
│    Port: 5432 (Docker Container)                        │
└──────────────────────────────────────────────────────────┘
```

### Componentes Principais

**Backend:**
- `AuthController` → JWT login/registro
- `TaskController` → CRUD + criar por prompt
- `JwtUtil` → Geração/validação HMAC-SHA256
- `SecurityConfig` → Spring Security stateless
- `EmailService` → Envio SMTP HTML
- `ClaudeService` → Processamento IA + data extraction
- `ReminderService` → Quartz scheduler

**Frontend:**
- `LoginPage` → Autenticação
- `Dashboard` → TaskList + AIChat
- `AIChat` → Interface de prompts
- `authService` → JWT management
- `taskService` → API calls

---

## 🛠️ Linguagens e Tecnologias

### Backend (Java/Spring)
| Componente | Versão | Uso |
|-----------|--------|-----|
| **Java** | 21 LTS | Linguagem principal |
| **Spring Boot** | 3.2.4 | Framework web |
| **Spring Security** | 6.2.3 | Autenticação JWT |
| **Spring Data JPA** | 3.2.4 | ORM (Hibernate) |
| **PostgreSQL Driver** | 42.6.2 | JDBC |
| **JJWT** | 0.12.6 | JWT (HMAC-SHA256) |
| **Jakarta Mail** | 2.0.3 | Email SMTP |
| **Quartz Scheduler** | 2.3.2 | Job agendamento |
| **Maven** | 3.9.11 | Build tool |
| **Lombok** | (latest) | Redução boilerplate |

### Frontend (JavaScript/React)
| Componente | Versão | Uso |
|-----------|--------|-----|
| **React** | 18.x | UI Library |
| **Vite** | 5.4.21 | Build tool |
| **Axios** | (latest) | HTTP client |
| **TailwindCSS** | 3.x | Styling |
| **React Router** | (latest) | Routing |
| **npm** | (latest) | Package manager |

### Infrastructure
| Componente | Versão | Uso |
|-----------|--------|-----|
| **PostgreSQL** | 15 | Database relacional |
| **Docker** | (latest) | Container |
| **Gmail SMTP** | - | Email delivery |

---

## 🎨 Design e Padrões

### Padrões de Arquitetura

#### **1. MVC (Model-View-Controller)**
```
Controllers → Services → Repositories → Entities (JPA)
      ↓
   JSON DTOs ← Frontend ← React Components
```

#### **2. Camadas de Segurança**
```
JwtAuthenticationFilter
        ↓
SecurityContextHolder
        ↓
@PreAuthorize/@Secured
        ↓
UserDetails carregado de UserRepository
```

#### **3. Fluxo de Autenticação JWT**
```
1. Usuário: email + senha
   ↓
2. AuthController.login() → passwordEncoder.matches()
   ↓
3. JwtUtil.generateToken(email) → JWT assinado HMAC-SHA256
   ↓
4. Response: { token, userId, email, name }
   ↓
5. Frontend: localStorage.setItem('token', jwt)
   ↓
6. Requisições: Authorization: Bearer <jwt>
   ↓
7. JwtAuthenticationFilter: extrai + valida token
   ↓
8. SecurityContext: autenticação confirmada
```

#### **4. Fluxo de Criação de Tarefa (com IA)**
```
User → Prompt: "Reunião amanhã às 14h"
       ↓
AIChat.jsx → POST /api/conversations/suggest-task
       ↓
ClaudeService.suggestTask(prompt, userId)
  ├─ extractDateFromMessage("amanhã às 14h")
  │  → Regex: "(\\d{1,2})(?::([0-5]\\d))?\\s*(?:h|horas?)"
  │  → LocalDateTime: 2026-04-28T14:00
  │  ├─ Extrai "amanhã" → now.plusDays(1)
  │  └─ Extrai "14h" → withHour(14).withMinute(0)
  │
  ├─ Identifica padrão: "reunião" → URGENT priority
  │
  └─ Response: AITaskSuggestion
       {
         suggestedTitle: "Participar da reunião",
         suggestedDescription: "Comparecer e participar ativamente",
         suggestedDueDate: "2026-04-28T14:00:00",
         suggestedPriority: "URGENT"
       }
       ↓
Frontend: exibe sugestão + botão "Criar Tarefa"
       ↓
User aprova → POST /api/tasks?userId=1
       ↓
TaskService.createTask()
  ├─ task = new Task()
  ├─ taskRepository.save(task)
  └─ sendTaskCreatedEmail(task)
       ├─ Build HTML email
       ├─ Helper.setText(htmlContent, true)
       └─ mailSender.send(message)
            ↓
       Email chegando em ~2 segundos!
```

#### **5. Padrão Repository (Spring Data JPA)**
```java
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserIdOrderByDueDateAsc(Long userId);
    // SQL gerado automaticamente pelo Spring
}

// Uso em Service
List<Task> tasks = taskRepository.findByUserIdOrderByDueDateAsc(userId);
```

#### **6. Padrão DTO (Data Transfer Object)**
```java
// Entity (pode ter relacionamentos complexos)
@Entity
public class Task {
    @ManyToOne
    private User user;  // ← não queremos expor no JSON
}

// DTO (apenas o que a API precisa)
@Data
public class TaskDTO {
    private Long id;
    private String title;
    // sem 'user' - mantém segurança
}
```

### Princípios SOLID Implementados

| Princípio | Implementação |
|-----------|---------------|
| **S**ingle Responsibility | TaskService ≠ EmailService ≠ ReminderService |
| **O**pen/Closed | Fácil adicionar novo tipo de reminder |
| **L**iskov Substitution | Spring Security interfaces polymórficas |
| **I**nterface Segregation | `TaskRepository` vs `UserRepository` |
| **D**ependency Inversion | Spring DI em vez de `new Service()` |

---

## 📦 Estrutura de Diretórios

```
TaskSchedulerAI/
│
├── backend/
│   ├── pom.xml                          ← Dependências Maven
│   ├── src/main/resources/
│   │   └── application.yml              ← Configuração (BD, JWT, Email)
│   │
│   └── src/main/java/com/taskscheduler/
│       ├── TaskSchedulerApplication.java ← @SpringBootApplication + CommandLineRunner
│       │
│       ├── controller/
│       │   ├── AuthController.java       # POST /api/auth/login, /register
│       │   ├── TaskController.java       # CRUD /api/tasks + /tasks/from-prompt
│       │   ├── ConversationController.java # POST /api/conversations/suggest-task
│       │   └── UserController.java       # GET /api/users/{id}
│       │
│       ├── service/
│       │   ├── TaskService.java          # createTask(), updateTask(), etc + email
│       │   ├── ClaudeService.java        # suggestTask() + extractDateFromMessage()
│       │   ├── EmailService.java         # sendTaskCreatedEmail(), buildHtmlContent()
│       │   ├── ReminderService.java      # checkAndSendReminders() (Quartz)
│       │   └── RestTemplateConfig.java   # HTTP client config
│       │
│       ├── security/
│       │   ├── JwtUtil.java              # generateToken(), extractEmail(), isTokenValid()
│       │   ├── JwtAuthenticationFilter.java # OncePerRequestFilter
│       │   └── UserDetailsServiceImpl.java  # loadUserByUsername()
│       │
│       ├── config/
│       │   ├── SecurityConfig.java       # @Configuration + SecurityFilterChain
│       │   └── QuartzConfig.java         # JobDetail + Trigger beans
│       │
│       ├── entity/                       ← JPA @Entity
│       │   ├── User.java                 # id, email, name, password, createdAt
│       │   ├── Task.java                 # id, user, title, dueDate, priority, status
│       │   ├── AiConversation.java       # id, user, userMessage, assistantResponse
│       │   └── ReminderLog.java          # id, task, reminderType, sentAt
│       │
│       ├── dto/                          ← Data Transfer Objects
│       │   ├── LoginRequest.java
│       │   ├── LoginResponse.java        # token, userId, email, name
│       │   ├── RegisterRequest.java
│       │   ├── CreateTaskRequest.java
│       │   ├── CreateTaskFromPromptRequest.java
│       │   ├── TaskDTO.java
│       │   ├── AITaskSuggestion.java
│       │   └── ConversationDTO.java
│       │
│       ├── repository/                   ← Spring Data JPA
│       │   ├── UserRepository.java       # findByEmail()
│       │   ├── TaskRepository.java       # findByUserIdOrderByDueDateAsc()
│       │   ├── AiConversationRepository.java
│       │   └── ReminderLogRepository.java
│       │
│       ├── job/
│       │   └── ReminderJob.java          # implements Quartz Job
│       │
│       └── exception/
│           ├── GlobalExceptionHandler.java
│           └── ResourceNotFoundException.java
│
├── frontend/
│   ├── package.json                      ← npm dependencies
│   ├── vite.config.js                    ← Proxy: /api → :8080
│   ├── index.html
│   ├── tailwind.config.js
│   │
│   └── src/
│       ├── main.jsx                      ← Entry point
│       ├── App.jsx                       ← Router + Layout
│       │
│       ├── pages/
│       │   ├── LoginPage.jsx             # Register + Login form
│       │   ├── Dashboard.jsx             # Main: TaskList + AIChat
│       │   └── ConversationsPage.jsx     # Histórico de IA
│       │
│       ├── components/
│       │   ├── TaskForm.jsx              # Form criação manual
│       │   ├── TaskList.jsx              # Lista + filtros
│       │   ├── AIChat.jsx                # Prompt → Sugestão → Validação
│       │   └── ... (outros)
│       │
│       └── services/
│           ├── api.js                    # Axios + interceptadores
│           ├── authService.js            # JWT + login/logout
│           └── taskService.js            # API calls
│
├── docker-compose.yml                    # PostgreSQL 15 + pgAdmin
└── README.md                             # Este arquivo
```

---

## 🚀 Como Instalar e Rodar

### Pré-requisitos
- **Java 21+** (JDK)
- **Node.js 18+** e npm
- **Maven 3.9+**
- **Docker** + Docker Compose
- **Git**

### 1️⃣ Clonar e Preparar

```bash
git clone <seu-repo>
cd TaskSchedulerAI
```

### 2️⃣ Banco de Dados (PostgreSQL)

```bash
# Inicia container PostgreSQL
docker-compose up -d

# Verificar
docker-compose ps
docker ps | grep postgres
```

### 3️⃣ Backend (Spring Boot)

```bash
cd backend

# Build
mvn clean package -DskipTests

# Reinício local seguro: recompila, encerra o processo antigo em :8080 e sobe o jar atual
cd ..
./scripts/restart-backend.sh

# Ou com Maven (pare o processo antigo antes de subir outro):
pkill -f backend/target/task-scheduler-ai-1.0.0.jar || true
GMAIL_EMAIL="seu@gmail.com" GMAIL_PASSWORD="xxxx xxxx xxxx xxxx" \
  mvn spring-boot:run
```

**Backend em**: http://localhost:8080
- Health: `GET /actuator/health`
- Swagger: `GET /swagger-ui.html`

### 4️⃣ Frontend (React + Vite)

```bash
cd frontend

# Instalar
npm install

# Dev server (com proxy /api → :8080)
npm run dev
```

**Frontend em**: http://localhost:5173

---

## 📱 Como Usar

### Fluxo 1: Autenticação
```
1. Acesse http://localhost:5173
2. Registre: Email + Nome + Senha (min 6 caracteres)
3. Ou login: Email + Senha
4. JWT é salvo em localStorage
5. Todas as requisições levam Authorization: Bearer <token>
```

### Fluxo 2: Criar Tarefa (Manual)
```
Dashboard → "Adicionar Tarefa"
  ├─ Título: "Estudar React"
  ├─ Descrição: "Aprender Hooks"
  ├─ Data: 2026-05-01T14:00:00
  └─ Prioridade: HIGH
     ↓
"Criar"
  ↓
✓ Email de confirmação enviado!
```

### Fluxo 3: Criar Tarefa (Com IA)
```
Dashboard → "Assistente IA"
  ├─ Prompt: "Reunião amanhã às 14h"
  ├─ "Obter Sugestão da IA"
  │   ├─ Título: "Participar da reunião"
  │   ├─ Data: 2026-04-28T14:00:00 ← extraído!
  │   └─ Prioridade: URGENT
  │
  ├─ "Criar Tarefa" (validar antes)
  └─ ✓ Email enviado!
```

### Exemplos de Prompts
```
✓ "Estudar amanhã"
✓ "Reunião próxima segunda às 10:30"
✓ "Comprar itens até 5 de maio"
✓ "Fazer exercício hoje às 19:00"
✓ "Próximo fim de semana viajar"
```

---

## 🔐 Segurança

### Autenticação
- ✅ **BCrypt**: `$2a$10$...` (salt + hash)
- ✅ **JWT**: `eyJhbGc...` (HMAC-SHA256, 24h)
- ✅ **Stateless**: Sem sessão no servidor
- ✅ **CORS**: `localhost:5173` permitido
- ✅ **Bearer Token**: `Authorization: Bearer <token>`

### Proteção de Endpoints
```
PUBLIC:
  POST /api/auth/login        # Sem token
  POST /api/auth/register     # Sem token

PROTECTED (requer JWT):
  GET    /api/tasks           # @JwtRequired
  POST   /api/tasks           # @JwtRequired
  PUT    /api/tasks/{id}      # @JwtRequired
  DELETE /api/tasks/{id}      # @JwtRequired
  GET    /api/conversations   # @JwtRequired
  POST   /api/conversations/suggest-task  # @JwtRequired
```

### Validação
- ✅ Email único: `@UniqueEmail` (custom)
- ✅ Email válido: `@Email`
- ✅ Senha min 6: `@Size(min=6)`
- ✅ Campos obrigatórios: `@NotBlank`
- ✅ Datas futuras: `@Future`

### Dados Sensíveis
- 🔒 Senhas: BCrypt + salt
- 🔒 JWT Secret: Variável de ambiente
- 🔒 Gmail Password: Variável de ambiente
- 🔒 Database: Sem secrets em `.sql` ou `git`

---

## ✨ Features

### ✅ Implementado
- [x] Autenticação JWT (login + registro)
- [x] CRUD completo de tarefas
- [x] Sugestões de IA (mock + extensível)
- [x] **Extração inteligente de datas em português**
- [x] **Extração de horas ("14h", "10:30")**
- [x] Envio automático de emails em HTML
- [x] Lembretes por email (1h e 24h antes)
- [x] Histórico de conversas com IA
- [x] Dashboard responsivo (React + Tailwind)
- [x] Validação de formulários + errors
- [x] Tratamento de erros global (@ExceptionHandler)
- [x] Spring Security + CORS configurado
- [x] Quartz Scheduler para jobs
- [x] Swagger/OpenAPI para documentação

### 🔄 Futuro
- [ ] Integração Claude API real
- [ ] Compartilhamento de tarefas
- [ ] Notificações push
- [ ] Google Calendar sync
- [ ] Dark mode
- [ ] Autenticação social (Google, GitHub)
- [ ] Criptografia de dados sensíveis

---

## 📧 Configuração de Email (Gmail)

### 1. Habilitar 2FA
```
https://myaccount.google.com/security
→ "Verificação em 2 etapas"
→ Habilitar
```

### 2. Gerar "App Password"
```
https://myaccount.google.com/apppasswords
→ Selecionar "Mail" + "Windows Computer"
→ Google gera: xxxx xxxx xxxx xxxx (16 caracteres)
```

### 3. Usar no Projeto
```bash
export GMAIL_EMAIL="seu-email@gmail.com"
export GMAIL_PASSWORD="xxxx xxxx xxxx xxxx"
java -jar backend/target/task-scheduler-ai-1.0.0.jar
```

---

## 🧪 Testes Manuais

### Teste 1: Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "demo@example.com",
    "password": "demo123"
  }'
```

**Response**: `{ "token": "eyJhbGc...", "userId": 1, ... }`

### Teste 2: Criar Tarefa
```bash
TOKEN="eyJhbGc..."
curl -X POST "http://localhost:8080/api/tasks?userId=1" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Teste",
    "description": "Task de teste",
    "dueDate": "2026-05-01T14:00:00",
    "priority": "HIGH"
  }'
```

### Teste 3: IA
```bash
curl -X POST http://localhost:8080/api/conversations/suggest-task \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userMessage": "Reunião amanhã às 14h",
    "userId": 1
  }'
```

---

## 🐛 Troubleshooting

### Backend não inicia
```
❌ "Cannot connect to PostgreSQL"
✅ docker-compose ps
✅ docker-compose down && docker-compose up -d

❌ "Port 8080 already in use"
✅ pkill -9 java
✅ ou mude a porta em application.yml
✅ para testes locais do AI terminal, use `./scripts/restart-backend.sh` antes de validar
```

### Email não chega
```
❌ "MessagingException: 535-5.7.8 Username and Password not accepted"
✅ Usar "App Password" (16 caracteres), não senha comum
✅ Habilitar 2FA: https://myaccount.google.com/security

❌ "Email em spam"
✅ Verificar pasta Spam/Lixo no Gmail
```

### Frontend não conecta
```
❌ "POST /api/tasks 404 (Not Found)"
✅ Backend está rodando? curl http://localhost:8080/actuator/health
✅ Proxy configurado? vite.config.js: proxy: { '/api': { target: '...' } }
✅ Reiniciar: npm run dev
```

---

## 📚 Documentação

### Swagger/OpenAPI
```
http://localhost:8080/swagger-ui.html
- Documentação automática de todos os endpoints
- Teste direto via UI
```

### Variáveis de Ambiente
```bash
# Backend
DB_USERNAME=postgres               # padrão
DB_PASSWORD=postgres               # padrão
GMAIL_EMAIL=seu@gmail.com          # OBRIGATÓRIO para email
GMAIL_PASSWORD=xxxx xxxx xxxx xxxx # OBRIGATÓRIO (App Password)
JWT_SECRET=<64+ chars aleatórios>  # Padrão: hardcoded seguro
GROQ_API_KEY=                      # (Opcional)
```

---

## 🎉 Destaques Técnicos

### 🧠 Extração de Datas em Português
```java
// ClaudeService.extractDateFromMessage()
"Reunião amanhã às 14h"
  → LocalDateTime: 2026-04-28T14:00:00

"Estudar próxima segunda às 10:30"
  → LocalDateTime: 2026-05-04T10:30:00

"Comprar até 5 de maio"
  → LocalDateTime: 2026-05-05T10:00:00
```

### 📧 Emails Profissionais em HTML
```
Subject: Tarefa Criada: Reunião com cliente

┌─────────────────────────────────┐
│ ✓ Tarefa criada com sucesso!    │
├─────────────────────────────────┤
│ Título: Reunião com cliente     │
│ Descrição: Reunir com equipe    │
│ Vencimento: 28/04/2026 14:00    │
│ Prioridade: URGENT              │
└─────────────────────────────────┘
```

### 🔐 Autenticação Enterprise
```
Senha: "abc123"
  ↓ BCrypt (10 rounds + salt aleatório)
Hash: "$2a$10$eImiTXuWVxfaHNYY5..."
  ↓ Armazenado no banco

Token JWT:
  Header: { alg: "HS512", typ: "JWT" }
  Payload: { sub: "user@example.com", iat, exp }
  Signature: HMAC-SHA256(secret)
```

---

## 👨‍💼 Autor

**Felipe Suguiura**  
Email: felipesuguiura.pro@gmail.com  
GitHub: [seu-usuario](https://github.com)

---

## 📄 Licença

MIT License - Veja LICENSE para detalhes

---

## 🚀 Próximos Passos Sugeridos

1. **Tests Unitários** - JUnit 5 + Mockito
2. **Tests de Integração** - TestContainers
3. **CI/CD** - GitHub Actions
4. **Monitoring** - Prometheus + Grafana
5. **Cache** - Redis para tasks frequentes
6. **Real-time** - WebSocket para notificações

---

**Última atualização**: Abril 2026  
**Status**: ✅ Em Produção (MVP)
