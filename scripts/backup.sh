#!/bin/bash

# ===================================
# AI Notes Tool - Database Backup Script
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
BACKUP_DIR="${BACKUP_DIR:-/home/PHY/projects/ai-notes-tool/backup}"
RETENTION_DAYS="${BACKUP_RETENTION_DAYS:-30}"

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

# Check if .env file exists
if [ ! -f "${ENV_FILE}" ]; then
    log_error ".env file not found"
    exit 1
fi

# Load environment variables
export $(grep -v '^#' "${ENV_FILE}" | xargs)

# Create backup directory if not exists
mkdir -p "${BACKUP_DIR}"

# Generate timestamp
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_DATE="${BACKUP_DIR}/${TIMESTAMP}"

log_info "========================================="
log_info "Starting Backup - ${TIMESTAMP}"
log_info "========================================="

# Create temporary backup directory
TMP_BACKUP_DIR="${BACKUP_DIR}/tmp_${TIMESTAMP}"
mkdir -p "${TMP_BACKUP_DIR}"

# Change to docker directory
cd "${PROJECT_DIR}/docker" || exit 1

# ===================================
# Step 1: Backup MySQL
# ===================================
log_info "Step 1: Backing up MySQL database..."

MYSQL_CONTAINER=$(docker-compose ps -q mysql)
if [ -z "${MYSQL_CONTAINER}" ]; then
    log_error "MySQL container is not running"
    exit 1
fi

MYSQL_DUMP_FILE="${TMP_BACKUP_DIR}/mysql_backup.sql"
docker exec "${MYSQL_CONTAINER}" mysqldump \
    -u"${MYSQL_USER:-root}" \
    -p"${MYSQL_ROOT_PASSWORD:-root123456}" \
    "${MYSQL_DATABASE:-ai_notes}" \
    --single-transaction \
    --quick \
    --lock-tables=false \
    > "${MYSQL_DUMP_FILE}"

if [ $? -eq 0 ]; then
    log_info "MySQL backup completed: ${MYSQL_DUMP_FILE}"
    BACKUP_SIZE=$(du -h "${MYSQL_DUMP_FILE}" | cut -f1)
    log_info "Backup size: ${BACKUP_SIZE}"
else
    log_error "MySQL backup failed"
    exit 1
fi

# Compress MySQL backup
gzip "${MYSQL_DUMP_FILE}"
log_info "MySQL backup compressed: ${MYSQL_DUMP_FILE}.gz"

# ===================================
# Step 2: Backup MinIO
# ===================================
log_info "Step 2: Backing up MinIO data..."

MINIO_CONTAINER=$(docker-compose ps -q minio)
if [ -z "${MINIO_CONTAINER}" ]; then
    log_warn "MinIO container is not running, skipping MinIO backup"
else
    MINIO_DATA_DIR="${TMP_BACKUP_DIR}/minio_data"
    mkdir -p "${MINIO_DATA_DIR}"

    docker cp "${MINIO_CONTAINER}:/data" "${MINIO_DATA_DIR}/" 2>/dev/null || {
        log_warn "Failed to copy MinIO data, skipping MinIO backup"
    }

    if [ -d "${MINIO_DATA_DIR}/data" ] && [ "$(ls -A ${MINIO_DATA_DIR}/data)" ]; then
        log_info "MinIO backup completed: ${MINIO_DATA_DIR}"
        tar -czf "${TMP_BACKUP_DIR}/minio_backup.tar.gz" -C "${MINIO_DATA_DIR}" data
        rm -rf "${MINIO_DATA_DIR}"
        log_info "MinIO backup compressed: ${TMP_BACKUP_DIR}/minio_backup.tar.gz"
    else
        log_warn "MinIO data is empty, skipping MinIO backup"
    fi
fi

# ===================================
# Step 3: Backup Redis (Optional)
# ===================================
log_info "Step 3: Backing up Redis data..."

REDIS_CONTAINER=$(docker-compose ps -q redis)
if [ -z "${REDIS_CONTAINER}" ]; then
    log_warn "Redis container is not running, skipping Redis backup"
else
    REDIS_DUMP_FILE="${TMP_BACKUP_DIR}/redis_backup.rdb"

    # Trigger Redis to save
    docker exec "${REDIS_CONTAINER}" redis-cli -a "${REDIS_PASSWORD:-redis123456}" --no-auth-warning BGSAVE >/dev/null 2>&1 || true

    # Wait a bit for BGSAVE to complete
    sleep 2

    docker cp "${REDIS_CONTAINER}:/data/dump.rdb" "${REDIS_DUMP_FILE}" 2>/dev/null || {
        log_warn "Failed to copy Redis dump, skipping Redis backup"
    }

    if [ -f "${REDIS_DUMP_FILE}" ]; then
        log_info "Redis backup completed: ${REDIS_DUMP_FILE}"
        gzip "${REDIS_DUMP_FILE}"
        log_info "Redis backup compressed: ${REDIS_DUMP_FILE}.gz"
    else
        log_warn "Redis dump file not found, skipping Redis backup"
    fi
fi

# ===================================
# Step 4: Create archive
# ===================================
log_info "Step 4: Creating backup archive..."

ARCHIVE_FILE="${BACKUP_DIR}/ai_notes_backup_${TIMESTAMP}.tar.gz"
tar -czf "${ARCHIVE_FILE}" -C "${TMP_BACKUP_DIR}" .

if [ $? -eq 0 ]; then
    log_info "Backup archive created: ${ARCHIVE_FILE}"
    ARCHIVE_SIZE=$(du -h "${ARCHIVE_FILE}" | cut -f1)
    log_info "Archive size: ${ARCHIVE_SIZE}"
else
    log_error "Failed to create backup archive"
    exit 1
fi

# Remove temporary directory
rm -rf "${TMP_BACKUP_DIR}"

# ===================================
# Step 5: Clean old backups
# ===================================
log_info "Step 5: Cleaning up old backups (older than ${RETENTION_DAYS} days)..."

# Find and delete old backup files
OLD_BACKUPS=$(find "${BACKUP_DIR}" -name "ai_notes_backup_*.tar.gz" -type f -mtime +${RETENTION_DAYS})

if [ -n "${OLD_BACKUPS}" ]; then
    echo "${OLD_BACKUPS}" | while read -r file; do
        log_info "Deleting old backup: ${file}"
        rm -f "${file}"
    done
    log_info "Old backups cleaned up"
else
    log_info "No old backups to clean up"
fi

# ===================================
# Summary
# ===================================
log_info "========================================="
log_info "Backup Summary"
log_info "========================================="
log_info "Backup file: ${ARCHIVE_FILE}"
log_info "Backup size: ${ARCHIVE_SIZE}"
log_info "Retention: ${RETENTION_DAYS} days"
log_info ""
log_info "To restore from backup:"
log_info "  tar -xzf ${ARCHIVE_FILE} -C /tmp"
log_info "  # Restore MySQL: gunzip /tmp/mysql_backup.sql.gz && docker exec -i ai-notes-mysql mysql -uroot -p ai_notes < /tmp/mysql_backup.sql"
log_info "  # Restore MinIO: docker cp /tmp/minio_data ai-notes-minio:/"
log_info "  # Restore Redis: docker cp /tmp/redis_backup.rdb ai-notes-redis:/data/dump.rdb"
log_info ""
log_info "✓ Backup completed successfully!"
