#!/bin/bash

# ===================================
# AI Notes Tool - Development Environment Script
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

# Change to project directory
cd "${PROJECT_DIR}" || exit 1
log_info "Changed to project directory: ${PROJECT_DIR}"

# Check if .env file exists
if [ ! -f "${ENV_FILE}" ]; then
    log_error ".env file not found. Please create it from .env.example"
    exit 1
fi

# Load environment variables
export $(grep -v '^#' "${ENV_FILE}" | xargs)

log_info "========================================="
log_info "Starting Development Environment"
log_info "========================================="
log_info "This will start MySQL, Redis, and MinIO only"
log_info "Backend and Frontend should be run locally"
log_info ""

# Start only infrastructure services
cd "${PROJECT_DIR}/docker" || exit 1

log_info "Starting infrastructure services..."
docker-compose up -d mysql redis minio

log_info ""
log_info "Waiting for services to be ready..."

# Function to check service health
check_service_health() {
    local service=$1
    local max_attempts=30
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
    log_warn "$service may still be starting up"
    return 0
}

# Check each service
check_service_health "mysql"
check_service_health "redis"
check_service_health "minio"

log_info ""
log_info "========================================="
log_info "Development Environment Ready"
log_info "========================================="
log_info "MySQL: jdbc:mysql://localhost:${MYSQL_PORT:-3306}/${MYSQL_DATABASE:-ai_notes}"
log_info "Redis: localhost:${REDIS_PORT:-6379} (password: ${REDIS_PASSWORD:-redis123456})"
log_info "MinIO: http://localhost:${MINIO_PORT:-9000}"
log_info "MinIO Console: http://localhost:${MINIO_CONSOLE_PORT:-9001} (admin/${MINIO_PASSWORD:-admin123456})"
log_info ""
log_info "To start backend locally:"
log_info "  cd ${PROJECT_DIR}"
log_info "  ./mvnw spring-boot:run"
log_info ""
log_info "To start frontend locally:"
log_info "  cd ${PROJECT_DIR}"
log_info "  npm run dev"
log_info ""
log_info "To stop services:"
log_info "  docker-compose down"
log_info ""
log_info "To view logs:"
log_info "  docker-compose logs -f mysql redis minio"
