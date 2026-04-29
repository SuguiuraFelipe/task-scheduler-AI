#!/bin/bash

# Task Scheduler AI - Startup Script
# Executa todo o projeto em 3 terminais

set -e

PROJECT_ROOT="/Users/felipesuguiura/Portifólio/TaskSchedulerAI"
BACKEND_DIR="$PROJECT_ROOT/backend"
FRONTEND_DIR="$PROJECT_ROOT/frontend"

echo "======================================"
echo "🚀 Task Scheduler AI - MVP Startup"
echo "======================================"
echo ""

# Função para verificar se comando existe
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Verificar pré-requisitos
echo "📋 Verificando pré-requisitos..."
for cmd in java mvn docker npm; do
    if command_exists "$cmd"; then
        echo "  ✅ $cmd"
    else
        echo "  ❌ $cmd não encontrado"
        exit 1
    fi
done
echo ""

# 0. Load .env
if [ -f "$PROJECT_ROOT/.env" ]; then
    set -a
    source "$PROJECT_ROOT/.env"
    set +a
    echo "🔑 Variáveis de ambiente carregadas do .env"
else
    echo "⚠️  Arquivo .env não encontrado — emails e IA podem não funcionar"
    echo "   Crie o .env a partir do .env.example"
fi
echo ""

# 1. Docker Compose
echo "🐘 Iniciando PostgreSQL (Docker)..."
cd "$PROJECT_ROOT"
docker-compose up -d > /dev/null 2>&1

# Aguardar banco ficar healthy
echo "   Aguardando PostgreSQL ficar pronto..."
sleep 3
for i in {1..30}; do
    if docker exec taskscheduler-postgres pg_isready -U postgres >/dev/null 2>&1; then
        echo "   ✅ PostgreSQL pronto!"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "   ❌ Timeout aguardando PostgreSQL"
        exit 1
    fi
    sleep 1
done
echo ""

# 2. Backend Build
echo "🔨 Compilando Backend (Maven)..."
cd "$BACKEND_DIR"
if [ ! -f "target/task-scheduler-ai-1.0.0.jar" ]; then
    mvn clean package -DskipTests -q
    echo "   ✅ Build concluído!"
else
    echo "   ✅ JAR já existe, pulando build"
fi
echo ""

# 3. Frontend Install
echo "📦 Instalando dependências Frontend (npm)..."
cd "$FRONTEND_DIR"
if [ ! -d "node_modules" ]; then
    npm install --silent
    echo "   ✅ Dependências instaladas!"
else
    echo "   ✅ node_modules já existe, pulando install"
fi
echo ""

# 4. Mostrar instruções
echo "======================================"
echo "✅ Setup Concluído!"
echo "======================================"
echo ""
echo "🎯 Próximos Passos:"
echo ""
echo "Terminal 1 (PostgreSQL):"
echo "  $ docker-compose ps"
echo "  # Verifique se taskscheduler-postgres está 'healthy'"
echo ""
echo "Terminal 2 (Backend - copie e execute):"
echo "  $ cd $PROJECT_ROOT"
echo "  $ ./scripts/restart-backend.sh"
echo ""
echo "Terminal 3 (Frontend - copie e execute):"
echo "  $ cd $FRONTEND_DIR"
echo "  $ npm run dev"
echo ""
echo "📱 Acesse:"
echo "  Frontend: http://localhost:5173"
echo "  Backend:  http://localhost:8080"
echo "  Swagger:  http://localhost:8080/swagger-ui.html"
echo ""
echo "======================================"
echo ""
echo "💡 Para parar tudo:"
echo "  docker-compose down"
echo "  pkill -f 'java -jar'"
echo "  pkill -f 'vite'"
echo ""
