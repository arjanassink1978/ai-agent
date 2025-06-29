#!/bin/bash
set -e

# Always run from the script's directory (project root)
cd "$(dirname "$0")"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Print functions
function print_success() {
  echo -e "${GREEN}✅ $1${NC}"
}

function print_warning() {
  echo -e "${YELLOW}⚠️  $1${NC}"
}

function print_error() {
  echo -e "${RED}❌ $1${NC}"
}

function print_status() {
  echo -e "${BLUE}ℹ️  $1${NC}"
}

function wait_for_port_free() {
  local port=$1
  local name=$2
  local timeout=20
  local waited=0
  while lsof -i :$port >/dev/null 2>&1; do
    if [ $waited -eq 0 ]; then
      echo -e "${YELLOW}⏳ Waiting for $name (port $port) to be free...${NC}"
    fi
    sleep 1
    waited=$((waited+1))
    if [ $waited -ge $timeout ]; then
      print_error "Timeout waiting for $name (port $port) to be free!"
      exit 1
    fi
  done
}

function kill_processes() {
  echo -e "${YELLOW}🔄 Killing existing processes...${NC}"
  pkill -f "AiAgentApplication" 2>/dev/null || true
  pkill -f "next dev" 2>/dev/null || true
  sleep 2
}

function cleanup() {
    print_status "Shutting down services..."
    pkill -f "AiAgentApplication" 2>/dev/null || true
    pkill -f "next dev" 2>/dev/null || true
    print_success "All services stopped."
}

# Set up signal handlers
trap cleanup SIGINT SIGTERM

echo -e "${GREEN}🚀 Starting AI Agent Services...${NC}"
kill_processes

wait_for_port_free 8080 "backend"
wait_for_port_free 3000 "frontend"

# Build backend
(
  echo -e "${YELLOW}🔧 Building backend...${NC}"
  cd backend && mvn clean package -DskipTests
)

# Start backend
(
  echo -e "${YELLOW}🔧 Starting backend...${NC}"
  cd backend && nohup mvn spring-boot:run > ../backend.log 2>&1 &
)
echo -e "${YELLOW}⏳ Waiting for backend to start...${NC}"
for i in {1..20}; do
  if curl -s http://localhost:8080/api/models >/dev/null; then
    print_success "Backend started!"
    break
  fi
  sleep 1
done

# Start frontend
(
  echo -e "${YELLOW}🔧 Starting frontend...${NC}"
  cd frontend && nohup npm run dev > ../frontend.log 2>&1 &
)
echo -e "${YELLOW}⏳ Waiting for frontend to start...${NC}"
for i in {1..20}; do
  if curl -s http://localhost:3000 >/dev/null; then
    print_success "Frontend started!"
    break
  fi
  sleep 1
done

print_success "All services started!"
echo -e "${YELLOW}Backend log: backend.log"
echo -e "Frontend log: frontend.log${NC}"
echo -e "${GREEN}Open http://localhost:3000 in your browser.${NC}"

echo ""
echo "================================================"
print_success "AI Agent System Started Successfully!"
echo "================================================"
echo ""
echo "🌐 Services:"
echo "   Frontend:    http://localhost:3000"
echo "   Backend:     http://localhost:8080"
echo "   H2 Console:  http://localhost:8080/h2-console"
echo ""
echo "📋 Log Files:"
echo "   Backend:     ./backend.log"
echo "   Frontend:    ./frontend.log"
echo ""
echo "🛑 To stop all services, run: ./stop-all.sh"
echo ""

# Keep script running
print_status "All services are running. Press Ctrl+C to stop."
while true; do
    sleep 10
done 