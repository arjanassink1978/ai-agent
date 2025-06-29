#!/bin/bash

# AI Agent - Stop All Services Script

echo "🛑 Stopping AI Agent Services"
echo "=============================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Function to kill processes on specific ports
kill_port() {
    local port=$1
    local pids=$(lsof -ti:$port 2>/dev/null)
    if [ ! -z "$pids" ]; then
        print_status "Stopping processes on port $port: $pids"
        kill -9 $pids 2>/dev/null || true
    else
        print_warning "No processes found on port $port"
    fi
}

print_status "Stopping backend (port 8080)..."
kill_port 8080

print_status "Stopping frontend (port 3000)..."
kill_port 3000

# Remove .pid files if present
rm -f .backend.pid .frontend.pid

print_success "All services stopped."

# Clean up log files
print_status "Cleaning up log files..."
rm -f backend.log frontend.log

print_success "All services stopped and cleaned up!" 