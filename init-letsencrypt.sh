#!/usr/bin/env bash
# Bootstrap Let's Encrypt certificate for the configured domain.
# Run ONCE before the first `docker compose up -d`, after .env is filled in.
#
# What it does:
#   1. Creates a temporary self-signed cert so nginx can start with HTTPS config
#   2. Starts nginx
#   3. Deletes the dummy cert
#   4. Asks Let's Encrypt for a real cert via http-01 challenge
#   5. Reloads nginx so it picks up the new cert
#
# Re-running is safe: it will replace the cert.

set -euo pipefail

if [ ! -f .env ]; then
  echo "ERROR: .env not found. Copy .env.example -> .env and fill in values first."
  exit 1
fi

# shellcheck disable=SC1091
set -a; . ./.env; set +a

: "${APP_DOMAIN:?APP_DOMAIN not set in .env}"
: "${ACME_EMAIL:?ACME_EMAIL not set in .env}"

# Use --staging while testing to avoid Let's Encrypt rate limits (5 certs/week per domain).
# Set STAGING=0 in .env (or unset) for real certificates.
STAGING=${STAGING:-0}
STAGING_FLAG=""
if [ "$STAGING" != "0" ]; then
  STAGING_FLAG="--staging"
  echo "==> Using Let's Encrypt STAGING (test certs, browser will warn)"
fi

DATA_PATH="./certbot-bootstrap"
CERT_PATH="/etc/letsencrypt/live/$APP_DOMAIN"

echo "==> Step 1/5: Creating dummy certificate so nginx can boot"
docker compose run --rm --entrypoint "\
  sh -c 'mkdir -p $CERT_PATH && \
  openssl req -x509 -nodes -newkey rsa:2048 -days 1 \
    -keyout $CERT_PATH/privkey.pem \
    -out $CERT_PATH/fullchain.pem \
    -subj \"/CN=localhost\"'" certbot

echo "==> Step 2/5: Starting nginx with dummy cert"
docker compose up -d nginx

echo "==> Step 3/5: Deleting dummy cert"
docker compose run --rm --entrypoint "\
  sh -c 'rm -rf /etc/letsencrypt/live/$APP_DOMAIN \
    /etc/letsencrypt/archive/$APP_DOMAIN \
    /etc/letsencrypt/renewal/$APP_DOMAIN.conf'" certbot

echo "==> Step 4/5: Requesting real cert from Let's Encrypt for $APP_DOMAIN"
docker compose run --rm --entrypoint "\
  certbot certonly --webroot -w /var/www/certbot \
    $STAGING_FLAG \
    --email $ACME_EMAIL \
    -d $APP_DOMAIN \
    --rsa-key-size 2048 \
    --agree-tos \
    --non-interactive \
    --force-renewal" certbot

echo "==> Step 5/5: Reloading nginx"
docker compose exec nginx nginx -s reload

echo
echo "Done. Cert installed for $APP_DOMAIN."
echo "Now run:  docker compose up -d"
