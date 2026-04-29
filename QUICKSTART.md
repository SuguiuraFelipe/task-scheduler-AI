# 🚀 Quick Start - Task Scheduler AI MVP

## ⏱️ Tempo Total: ~5 minutos

---

## 📋 Pré-requisitos

```bash
# Verifique se tem tudo instalado:
java -version          # Java 21+
mvn -version           # Maven 3.8+
docker --version       # Docker
docker-compose --version
node --version         # Node 18+
npm --version
```

---

## 🏃 Execução Rápida (3 Terminais)

### **Terminal 1: Banco de Dados**

```bash
cd /Users/felipesuguiura/Portifólio/TaskSchedulerAI

# Inicie PostgreSQL via Docker
docker-compose up -d

# Verifique se está saudável
docker-compose ps
# Deve mostrar: taskscheduler-postgres ... (healthy)
```

✅ **Status**: PostgreSQL rodando em `localhost:5432`

---

### **Terminal 2: Backend (Spring Boot)**

```bash
cd /Users/felipesuguiura/Portifólio/TaskSchedulerAI/backend

# Build (primeira vez)
mvn clean package -DskipTests -q

# Execute
java -jar target/task-scheduler-ai-1.0.0.jar
```

Aguarde até ver:
```
Started TaskSchedulerApplication in X seconds
Demo user created with ID: 1
Scheduler TaskSchedulerQuartz_$_NON_CLUSTERED started.
```

✅ **Status**: API rodando em `http://localhost:8080`

---

### **Terminal 3: Frontend (React + Vite)**

```bash
cd /Users/felipesuguiura/Portifólio/TaskSchedulerAI/frontend

# Instale dependências (primeira vez)
npm install

# Inicie servidor de desenvolvimento
npm run dev
```

Aguarde até ver:
```
Local:   http://localhost:5173/
```

✅ **Status**: Frontend rodando em `http://localhost:5173`

---

## 🧪 Testes Manuais

### **1. Dashboard (Frontend)**

Abra no navegador:
```
http://localhost:5173
```

Deve aparecer:
- ✅ Navbar com "Task Scheduler AI"
- ✅ Campo para User ID (com valor 1 preenchido)
- ✅ Formulário "Nova Tarefa"
- ✅ Seção "Assistente IA"
- ✅ Seção "Minhas Tarefas" (vazia no início)

---

### **2. Criar Tarefa (Manual)**

No formulário "Nova Tarefa":

1. **Título**: "Comprar leite"
2. **Descrição**: "Ir ao mercado e comprar leite desnatado"
3. **Data de Vencimento**: Selecione uma data/hora no futuro
4. **Prioridade**: "Alta"
5. Clique em **"Criar Tarefa"**

Resultado esperado:
- ✅ Tarefa aparece na lista abaixo
- ✅ Mostra titulo, descrição, data e prioridade
- ✅ Botão "Deletar" disponível

---

### **3. Testar Assistente IA (Claude)**

⚠️ **Requer**: Variável de ambiente `CLAUDE_API_KEY`

Se você tiver a chave:

```bash
# Parar o backend (Ctrl+C no Terminal 2)

# Inicie com a chave
export CLAUDE_API_KEY=sk-ant-v3-sua-chave-aqui
java -jar target/task-scheduler-ai-1.0.0.jar
```

No frontend, na seção "Assistente IA":

1. Tipo: "Preciso estudar Spring Boot amanhã às 19h com urgência"
2. Clique em **"Sugerir Tarefa"**

Resultado esperado:
- ✅ Claude analisa o texto
- ✅ Aparece sugestão com título, descrição, data e prioridade
- ✅ Botão "Criar Tarefa" para aceitar a sugestão

---

### **4. Testar API (Swagger UI)**

Abra no navegador:
```
http://localhost:8080/swagger-ui.html
```

Você vai ver:
- ✅ Todos os endpoints listados
- ✅ Expandir qualquer endpoint e "Try it out"
- ✅ Testar sem sair do Swagger

**Endpoints principais:**

```
GET    /api/tasks?userId=1              → Listar tarefas
POST   /api/tasks?userId=1              → Criar tarefa
GET    /api/conversations?userId=1      → Histórico IA
POST   /api/conversations/suggest-task  → Sugerir com IA
```

---

### **5. Testar com cURL**

#### Listar tarefas:
```bash
curl 'http://localhost:8080/api/tasks?userId=1'
```

Resposta esperada:
```json
[
  {
    "id": 1,
    "userId": 1,
    "title": "Comprar leite",
    "description": "Ir ao mercado...",
    "dueDate": "2026-05-15T20:00:00",
    "status": "PENDING",
    "priority": "HIGH",
    "createdAt": "2026-04-27T...",
    "updatedAt": "2026-04-27T..."
  }
]
```

#### Criar tarefa:
```bash
curl -X POST 'http://localhost:8080/api/tasks?userId=1' \
  -H 'Content-Type: application/json' \
  -d '{
    "title": "Estudar React",
    "description": "Aprender hooks",
    "dueDate": "2026-05-20T18:00:00",
    "priority": "MEDIUM"
  }'
```

---

### **6. Testar Página de Conversas**

No frontend:
1. Clique em **"Conversas"** na navbar
2. Deve mostrar histórico de interações com IA
3. Cada conversa mostra: mensagem do usuário + resposta da IA

---

### **7. Testar Lembretes (Quartz)**

Os lembretes são agendados automaticamente a cada **1 hora**.

Para forçar execução (dentro do código):
- O job procura tarefas vencendo em **1 dia** ou **1 hora**
- Envia emails via Gmail (se configurado)

Log esperado no Terminal 2:
```
Starting reminder check job
Reminder check job completed
```

---

## 🔧 Configurações Importantes

### **Variáveis de Ambiente (Opcionais)**

Crie arquivo `.env` na raiz do projeto:

```bash
# PostgreSQL (padrão já funciona)
DB_URL=jdbc:postgresql://localhost:5432/taskscheduler
DB_USERNAME=postgres
DB_PASSWORD=postgres

# Gmail (opcional, para lembretes por email)
GMAIL_EMAIL=seu-email@gmail.com
GMAIL_PASSWORD=sua-senha-app

# Claude API (opcional, para assistente IA)
CLAUDE_API_KEY=sk-ant-v3-sua-chave-aqui
```

Depois rode o backend com:
```bash
source .env
java -jar backend/target/task-scheduler-ai-1.0.0.jar
```

---

## ⚠️ Troubleshooting

### Porta 5173 em uso
```bash
lsof -i :5173
kill -9 <PID>
npm run dev
```

### Porta 8080 em uso
```bash
lsof -i :8080
kill -9 <PID>
java -jar backend/target/task-scheduler-ai-1.0.0.jar
```

### Porta 5432 em uso
```bash
docker-compose down
docker-compose up -d
```

### Database não existe
```bash
docker exec taskscheduler-postgres psql -U postgres -c "CREATE DATABASE taskscheduler;"
```

### Maven compilation error
```bash
cd backend
mvn clean install -DskipTests
```

### npm install lento
```bash
npm install --prefer-offline --no-audit
```

---

## 📸 Fluxo Completo de Teste

```
1. Abrir http://localhost:5173 no navegador
   ↓
2. Ver Dashboard com formulário e IA (User ID = 1)
   ↓
3. Criar tarefa manualmente OU descrever em linguagem natural
   ↓
4. Se IA: Claude sugere estrutura → Você aceita
   ↓
5. Tarefa salva e aparece na lista
   ↓
6. Clicar em "Conversas" para ver histórico IA
   ↓
7. Abrir Swagger em http://localhost:8080/swagger-ui.html
   ↓
8. Testar endpoints via Swagger ou cURL
```

---

## 🎯 O que Demonstrar na Entrevista

✅ **Funcionalidades Core:**
- CRUD de tarefas (criar, listar, deletar)
- Integração Claude API (descrição natural → tarefa)
- Histórico de conversas salvo
- API RESTful documentada (Swagger)

✅ **Tech Stack:**
- Spring Boot 3 + PostgreSQL
- React 18 + Vite
- Quartz Scheduler (agendamento)
- Claude AI (processamento)

✅ **Código Clean:**
- Estrutura profissional (controllers, services, entities)
- DTOs para comunicação
- Tratamento de erros global
- Logging adequado

---

## 🚀 Comandos Úteis

```bash
# Parar tudo
docker-compose down
pkill -f "java -jar"
pkill -f "vite"

# Limpar e rebuild
cd backend && mvn clean && mvn package -DskipTests
cd ../frontend && rm -rf node_modules && npm install

# Logs em tempo real
tail -f /tmp/spring-boot.log
```

---

**Pronto? Bora testar! 🎉**
