#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
MCP_PORT=8081
MAIN_PORT=8080
FRONTEND_PORT=3000
MCP_SERVER_DIR="mcp-server"
MAIN_APP_DIR="."
FRONTEND_DIR="frontend"

echo -e "${BLUE}🚀 Starting AI Agent with GitHub MCP Integration${NC}"
echo "=================================================="

# Function to check if a port is in use
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null ; then
        return 0
    else
        return 1
    fi
}

# Function to stop services on a port
stop_port() {
    local port=$1
    local service_name=$2
    
    if check_port $port; then
        echo -e "${YELLOW}🛑 Stopping existing service on port $port...${NC}"
        local pids=$(lsof -ti :$port)
        if [ ! -z "$pids" ]; then
            echo "Found PIDs: $pids"
            kill -9 $pids 2>/dev/null
            sleep 2
            if check_port $port; then
                echo -e "${RED}❌ Failed to stop service on port $port${NC}"
                return 1
            else
                echo -e "${GREEN}✅ Successfully stopped service on port $port${NC}"
                return 0
            fi
        fi
    fi
    return 0
}

# Function to stop all required ports
stop_all_ports() {
    echo -e "${YELLOW}🔍 Checking for existing services on required ports...${NC}"
    
    local ports_to_stop=($MCP_PORT $MAIN_PORT $FRONTEND_PORT)
    local stopped_any=false
    
    for port in "${ports_to_stop[@]}"; do
        if check_port $port; then
            stop_port $port "Service on port $port"
            stopped_any=true
        fi
    done
    
    if [ "$stopped_any" = true ]; then
        echo -e "${GREEN}✅ All existing services stopped${NC}"
        sleep 3  # Give time for ports to be released
    else
        echo -e "${GREEN}✅ No existing services found on required ports${NC}"
    fi
}

# Function to wait for a service to be ready
wait_for_service() {
    local port=$1
    local service_name=$2
    local max_attempts=30
    local attempt=1
    
    echo -e "${YELLOW}⏳ Waiting for $service_name to be ready on port $port...${NC}"
    
    while [ $attempt -le $max_attempts ]; do
        if check_port $port; then
            echo -e "${GREEN}✅ $service_name is ready on port $port${NC}"
            return 0
        fi
        
        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    echo -e "\n${RED}❌ $service_name failed to start on port $port${NC}"
    return 1
}

# Function to cleanup background processes on exit
cleanup() {
    echo -e "\n${YELLOW}🛑 Shutting down services...${NC}"
    
    # Kill background processes
    if [ ! -z "$MCP_PID" ]; then
        echo "Stopping MCP Server (PID: $MCP_PID)"
        kill $MCP_PID 2>/dev/null
    fi
    
    if [ ! -z "$MAIN_PID" ]; then
        echo "Stopping Main Application (PID: $MAIN_PID)"
        kill $MAIN_PID 2>/dev/null
    fi

    if [ ! -z "$FRONTEND_PID" ]; then
        echo "Stopping Frontend (PID: $FRONTEND_PID)"
        kill $FRONTEND_PID 2>/dev/null
    fi
    
    # Clean up temporary files
    rm -f /tmp/mcp-server.log /tmp/main-app.log /tmp/frontend.log
    
    echo -e "${GREEN}✅ All services stopped${NC}"
    exit 0
}

# Set up signal handlers
trap cleanup SIGINT SIGTERM

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo -e "${RED}❌ Java is not installed. Please install Java 17 or higher.${NC}"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo -e "${RED}❌ Java 17 or higher is required. Current version: $JAVA_VERSION${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Java version check passed${NC}"

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}❌ Maven is not installed. Please install Maven.${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Maven is available${NC}"

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo -e "${RED}❌ Node.js is not installed. Please install Node.js.${NC}"
    exit 1
fi

# Check if npm is installed
if ! command -v npm &> /dev/null; then
    echo -e "${RED}❌ npm is not installed. Please install npm.${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Node.js and npm are available${NC}"

# Stop any existing services on required ports
stop_all_ports

# Verify ports are now available
echo -e "${BLUE}🔍 Verifying ports are available...${NC}"
if check_port $MCP_PORT; then
    echo -e "${RED}❌ Port $MCP_PORT is still in use after cleanup. Please manually stop the service.${NC}"
    exit 1
fi

if check_port $MAIN_PORT; then
    echo -e "${RED}❌ Port $MAIN_PORT is still in use after cleanup. Please manually stop the service.${NC}"
    exit 1
fi

if check_port $FRONTEND_PORT; then
    echo -e "${RED}❌ Port $FRONTEND_PORT is still in use after cleanup. Please manually stop the service.${NC}"
    exit 1
fi

echo -e "${GREEN}✅ All ports are available${NC}"

# Step 1: Build and start MCP Server
echo -e "\n${BLUE}Building MCP Server...${NC}"
cd $MCP_SERVER_DIR

if ! mvn clean package -DskipTests -q; then
    echo -e "${RED}Failed to build MCP Server${NC}"
    exit 1
fi

echo -e "${GREEN}MCP Server built successfully${NC}"

# Start MCP Server in background
echo -e "\n${BLUE}Starting MCP Server on port $MCP_PORT...${NC}"
nohup java -jar target/github-mcp-server-1.0.0.jar --server.port=$MCP_PORT > /tmp/mcp-server.log 2>&1 &
MCP_PID=$!

# Wait for MCP Server to be ready
if ! wait_for_service $MCP_PORT "MCP Server"; then
    echo -e "${RED}❌ MCP Server failed to start${NC}"
    cleanup
    exit 1
fi

# Step 2: Build and start Main Application
echo -e "\n${BLUE}📦 Building Main Application...${NC}"
cd ../$MAIN_APP_DIR

if ! mvn clean compile -q; then
    echo -e "${RED}❌ Failed to build Main Application${NC}"
    cleanup
    exit 1
fi

echo -e "${GREEN}✅ Main Application built successfully${NC}"

# Start Main Application in background
echo -e "\n${BLUE}🚀 Starting Main Application on port $MAIN_PORT...${NC}"
nohup mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dserver.port=$MAIN_PORT" > /tmp/main-app.log 2>&1 &
MAIN_PID=$!

# Wait for Main Application to be ready
if ! wait_for_service $MAIN_PORT "Main Application"; then
    echo -e "${RED}❌ Main Application failed to start${NC}"
    cleanup
    exit 1
fi

# Step 3: Build and start Frontend (Next.js)
echo -e "\n${BLUE}📦 Building Frontend (Next.js)...${NC}"
cd $FRONTEND_DIR

if ! npm install --legacy-peer-deps > /tmp/frontend.log 2>&1; then
    echo -e "${RED}❌ npm install failed in frontend${NC}"
    cleanup
    exit 1
fi

if ! npm run build >> /tmp/frontend.log 2>&1; then
    echo -e "${RED}❌ npm run build failed in frontend${NC}"
    cleanup
    exit 1
fi

echo -e "${GREEN}✅ Frontend built successfully${NC}"

# Start Frontend in background
echo -e "\n${BLUE}🚀 Starting Frontend on port $FRONTEND_PORT...${NC}"
nohup npm start > /tmp/frontend.log 2>&1 &
FRONTEND_PID=$!

# Wait for Frontend to be ready
if ! wait_for_service $FRONTEND_PORT "Frontend"; then
    echo -e "${RED}❌ Frontend failed to start${NC}"
    cleanup
    exit 1
fi

cd ..

# Success message
echo -e "\n${GREEN}🎉 All services started successfully!${NC}"
echo "=================================================="
echo -e "${BLUE}📱 Main Application:${NC} http://localhost:$MAIN_PORT"
echo -e "${BLUE}🔧 MCP Server:${NC} http://localhost:$MCP_PORT"
echo -e "${BLUE}🌐 Frontend:${NC} http://localhost:$FRONTEND_PORT"
echo -e "${BLUE}📋 MCP Server Logs:${NC} tail -f /tmp/mcp-server.log"
echo -e "${BLUE}📋 Main App Logs:${NC} tail -f /tmp/main-app.log"
echo -e "${BLUE}📋 Frontend Logs:${NC} tail -f /tmp/frontend.log"
echo ""
echo -e "${YELLOW}💡 Usage:${NC}"
echo "1. Open http://localhost:$FRONTEND_PORT in your browser"
echo "2. Go to the 'Coding Buddy' tab"
echo "3. Enter your GitHub Personal Access Token"
echo "4. Start chatting with natural language!"
echo ""
echo -e "${YELLOW}🛑 To stop all services:${NC} Press Ctrl+C"
echo ""

# Keep the script running and show logs
echo -e "${BLUE}📊 Live logs (Ctrl+C to stop):${NC}"
echo "=================================================="

# Function to show logs
show_logs() {
    while true; do
        clear
        echo -e "${BLUE}📊 Live Application Logs${NC}"
        echo "=================================================="
        echo -e "${GREEN}Main Application Logs:${NC}"
        tail -n 10 /tmp/main-app.log 2>/dev/null || echo "No logs yet..."
        echo ""
        echo -e "${GREEN}MCP Server Logs:${NC}"
        tail -n 10 /tmp/mcp-server.log 2>/dev/null || echo "No logs yet..."
        echo ""
        echo -e "${GREEN}Frontend Logs:${NC}"
        tail -n 10 /tmp/frontend.log 2>/dev/null || echo "No logs yet..."
        echo ""
        echo -e "${YELLOW}Press Ctrl+C to stop all services${NC}"
        sleep 3
    done
}

# Show logs until interrupted
show_logs 