#!/bin/bash

# ===================================
# AI Notes Tool - Production Deployment Script
# ===================================

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
PROJECT_DIR="/home/PHY/projects/ai-notes-tool"
DOCKER_COMPOSE_FILE="${PROJECT_DIR}/docker/docker-compose.yml"
ENV_FILE="${PROJECT_DIR}/.env"

# Functions
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if running as root
if [ "$EUID" -eq 0 ]; then
    log_warn "Running as root is not recommended"
fi

# Change to project directory
cd "${PROJECT_DIR}" || exit 1
log_info "Changed to project directory: ${PROJECT_DIR}"

# Check if .env file exists
if [ ! -f "${ENV_FILE}" ]; then
    log_error ".env file not found. Please create it from .env.example"
    exit 1
fi
log_info "Environment file found"

# Load environment variables
export $(grep -v '^#' "${ENV_FILE}" | xargs)

# Step 1: Pull latest code
log_info "========================================="
log_info "Step 1: Pulling latest code"
log_info "========================================="
git fetch origin
git reset --hard origin/main
log_info "Code updated successfully"

# Step 2: Stop existing containers
log_info "========================================="
log_info "Step 2: Stopping existing containers"
log_info "========================================="
cd "${PROJECT_DIR}/docker" || exit 1
docker-compose down || true
log_info "Containers stopped"

# Step 3: Build Docker images
log_info "========================================="
log_info "Step 3: Building Docker images"
log_info "========================================="
docker-compose build --no-cache
log_info "Docker images built successfully"

# Step 4: Start services
log_info "========================================="
log_info "Step 4: Starting services"
log_info "========================================="
docker-compose up -d
log_info "Services started"

# Step 5: Wait for services to be healthy
log_info "========================================="
log_info "Step 5: Health check - Waiting for services to be ready"
log_info "========================================="

# Function to check container health
check_service_health() {
    local service=$1
    local max_attempts=60
    local attempt=1

    while [ $attempt -le $max_attempts ]; do
        if docker-compose ps | grep "$service" | grep -q "healthy"; then
            log_info "$service is healthy ✓"
            return 0
        fi
        echo -n "."
        sleep 2
        ((attempt++))
    done
    echo
    log_error "$service failed health check"
    return 1
}

# Check each service
check_service_health "mysql"
check_service_health "redis"
check_service_health "minio"
check_service_health "backend"
check_service_health "frontend"

# Step 6: Final status
log_info "========================================="
log_info "Deployment Summary"
log_info "========================================="
log_info "MySQL: http://localhost:${MYSQL_PORT:-3306}"
log_info "Redis: localhost:${REDIS_PORT:-6379}"
log_info "MinIO: http://localhost:${MINIO_PORT:-9000}"
log_info "MinIO Console: http://localhost:${MINIO_CONSOLE_PORT:-9001}"
log_info "Backend API: http://localhost:${BACKEND_PORT:-8080}"
log_info "Frontend: http://localhost:${FRONTEND_PORT:-80}"
log_info ""
log_info "✓ Deployment completed successfully!"

# Display container status
log_info "Container Status:"
docker-compose ps

log_info "To view logs: docker-compose logs -f"
