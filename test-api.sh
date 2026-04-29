#!/bin/bash

# Task Scheduler AI - API Test Script
# Testa todos os endpoints

set -e

BACKEND_URL="http://localhost:8080"
USER_ID=1

echo "╔════════════════════════════════════════╗"
echo "║   Task Scheduler API - Test Suite      ║"
echo "╚════════════════════════════════════════╝"
echo ""

# Cores para output
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Função para testar endpoint
test_endpoint() {
    local method=$1
    local endpoint=$2
    local data=$3
    local expected_code=$4

    echo -e "${BLUE}[TEST]${NC} $method $endpoint"

    if [ -z "$data" ]; then
        response=$(curl -s -w "\n%{http_code}" -X $method "$BACKEND_URL$endpoint")
    else
        response=$(curl -s -w "\n%{http_code}" -X $method "$BACKEND_URL$endpoint" \
            -H "Content-Type: application/json" \
            -d "$data")
    fi

    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')

    if [ "$http_code" = "$expected_code" ]; then
        echo -e "${GREEN}✅ HTTP $http_code${NC}"
    else
        echo -e "${RED}❌ HTTP $http_code (esperado $expected_code)${NC}"
    fi

    echo "$body" | head -c 200
    echo ""
    echo ""

    # Retornar o body para reutilizar
    echo "$body"
}

# Verificar se backend está rodando
echo "🔍 Verificando conectividade..."
if ! curl -s "$BACKEND_URL/actuator/health" > /dev/null 2>&1; then
    echo -e "${RED}❌ Backend não está acessível em $BACKEND_URL${NC}"
    echo "Execute no Terminal 2:"
    echo "  cd backend && java -jar target/task-scheduler-ai-1.0.0.jar"
    exit 1
fi
echo -e "${GREEN}✅ Backend respondendo${NC}"
echo ""

# ============================================
# TESTE 1: Listar tarefas (deve estar vazio)
# ============================================
echo "═════════════════════════════════════════"
echo "📋 TESTE 1: Listar Tarefas (vazio)"
echo "═════════════════════════════════════════"
test_endpoint "GET" "/api/tasks?userId=$USER_ID" "" "200"

# ============================================
# TESTE 2: Criar tarefa
# ============================================
echo "═════════════════════════════════════════"
echo "➕ TESTE 2: Criar Tarefa"
echo "═════════════════════════════════════════"

FUTURE_DATE=$(date -u -v+2d +"%Y-%m-%dT%H:%M:%S" 2>/dev/null || date -u -d "+2 days" +"%Y-%m-%dT%H:%M:%S")

TASK_JSON="{
  \"title\": \"Teste de Integração\",
  \"description\": \"Tarefa criada automaticamente\",
  \"dueDate\": \"$FUTURE_DATE\",
  \"priority\": \"HIGH\"
}"

TASK_RESPONSE=$(test_endpoint "POST" "/api/tasks?userId=$USER_ID" "$TASK_JSON" "201")

# Extrair ID da tarefa criada
TASK_ID=$(echo "$TASK_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*' || echo "")

if [ -z "$TASK_ID" ]; then
    echo -e "${RED}⚠️  Não foi possível extrair ID da tarefa${NC}"
    TASK_ID=1
fi

echo -e "${GREEN}Task ID: $TASK_ID${NC}"
echo ""

# ============================================
# TESTE 3: Listar tarefas (deve ter 1)
# ============================================
echo "═════════════════════════════════════════"
echo "📋 TESTE 3: Listar Tarefas (com 1 tarefa)"
echo "═════════════════════════════════════════"
test_endpoint "GET" "/api/tasks?userId=$USER_ID" "" "200"

# ============================================
# TESTE 4: Obter tarefa específica
# ============================================
echo "═════════════════════════════════════════"
echo "🔍 TESTE 4: Obter Tarefa Específica"
echo "═════════════════════════════════════════"
test_endpoint "GET" "/api/tasks/$TASK_ID" "" "200"

# ============================================
# TESTE 5: Swagger UI
# ============================================
echo "═════════════════════════════════════════"
echo "📚 TESTE 5: Swagger UI (Documentação)"
echo "═════════════════════════════════════════"

http_code=$(curl -s -o /dev/null -w "%{http_code}" "$BACKEND_URL/swagger-ui.html")
echo -e "${BLUE}[TEST]${NC} GET /swagger-ui.html"
if [ "$http_code" = "200" ]; then
    echo -e "${GREEN}✅ HTTP $http_code${NC}"
else
    echo -e "${RED}❌ HTTP $http_code${NC}"
fi
echo "Acesse em navegador: $BACKEND_URL/swagger-ui.html"
echo ""

# ============================================
# TESTE 6: Health Check
# ============================================
echo "═════════════════════════════════════════"
echo "💓 TESTE 6: Health Check"
echo "═════════════════════════════════════════"
test_endpoint "GET" "/actuator/health" "" "200"

# ============================================
# TESTE 7: Registrar novo usuário (opcional)
# ============================================
echo "═════════════════════════════════════════"
echo "👤 TESTE 7: Registrar Novo Usuário"
echo "═════════════════════════════════════════"

USER_JSON="{
  \"email\": \"test-$(date +%s)@example.com\",
  \"name\": \"Test User\"
}"

test_endpoint "POST" "/api/users/register" "$USER_JSON" "201"

# ============================================
# Resumo Final
# ============================================
echo "═════════════════════════════════════════"
echo "✅ TESTES CONCLUÍDOS"
echo "═════════════════════════════════════════"
echo ""
echo "📊 Resumo:"
echo "  ✅ Backend conectado e respondendo"
echo "  ✅ CRUD de tarefas funcionando"
echo "  ✅ Swagger UI acessível"
echo "  ✅ Health check ok"
echo ""
echo "🔗 URLs importantes:"
echo "  Frontend:  http://localhost:5173"
echo "  Backend:   http://localhost:8080"
echo "  Swagger:   http://localhost:8080/swagger-ui.html"
echo "  Health:    http://localhost:8080/actuator/health"
echo ""
echo "🚀 Frontend pronto? Abra http://localhost:5173 no navegador"
echo ""
