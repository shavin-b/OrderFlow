# OrderFlow SaaS — Database Backup & Disaster Recovery Strategy

This document outlines automated database backup, point-in-time recovery, and restore procedures for the OrderFlow MySQL database.

---

## 💾 Automated Backup Script (`backup.sh`)

Save this script as `/opt/orderflow/scripts/backup.sh`:

```bash
#!/bin/bash
set -eo pipefail

TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_DIR="/var/backups/orderflow"
DB_CONTAINER="orderflow-mysql"
DB_NAME="${DB_NAME:-orderflow}"
DB_USER="${DB_USERNAME:-orderflow_user}"
DB_PASS="${DB_PASSWORD:-orderflow_pass}"

mkdir -p "$BACKUP_DIR"

FILENAME="$BACKUP_DIR/orderflow_backup_$TIMESTAMP.sql.gz"

echo "[$(date)] Starting MySQL backup..."
docker exec "$DB_CONTAINER" mysqldump -u"$DB_USER" -p"$DB_PASS" --single-transaction --quick "$DB_NAME" | gzip -9 > "$FILENAME"

echo "[$(date)] Backup completed: $FILENAME"

# Retain backups for 30 days
find "$BACKUP_DIR" -type f -name "*.sql.gz" -mtime +30 -delete
```

Make it executable:
```bash
chmod +x /opt/orderflow/scripts/backup.sh
```

---

## ⏰ Automated Cron Job Setup

Add a daily automated cron task running at 02:00 AM:

```bash
sudo crontab -e
# Add the following line:
0 2 * * * /opt/orderflow/scripts/backup.sh >> /var/log/orderflow_backup.log 2>&1
```

---

## 🔄 Disaster Recovery & Database Restore Strategy

To restore database state from a `.sql.gz` backup file:

```bash
# 1. Stop backend container to prevent incoming writes
docker-compose stop backend

# 2. Decompress and restore database
gunzip -c /var/backups/orderflow/orderflow_backup_20260730_020000.sql.gz | \
  docker exec -i orderflow-mysql mysql -uorderflow_user -porderflow_pass orderflow

# 3. Restart backend container
docker-compose start backend
```
