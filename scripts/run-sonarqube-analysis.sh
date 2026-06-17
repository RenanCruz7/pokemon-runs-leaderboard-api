#!/usr/bin/env bash
set -euo pipefail

SONAR_HOST_URL="${SONAR_HOST_URL:-http://localhost:9000}"
SONAR_TIMEOUT_SECONDS="${SONAR_TIMEOUT_SECONDS:-180}"

if [[ -z "${SONAR_TOKEN:-}" ]]; then
  printf 'SONAR_TOKEN nao definido. Exporte um token antes de rodar este script.\n' >&2
  exit 1
fi

printf 'Subindo SonarQube local...\n'
docker compose up -d sonarqube-db sonarqube

printf 'Aguardando SonarQube ficar pronto em %s ...\n' "$SONAR_HOST_URL"
elapsed=0
until curl -fsS "$SONAR_HOST_URL/api/system/status" >/tmp/opencode/sonar-status.json 2>/dev/null; do
  if (( elapsed >= SONAR_TIMEOUT_SECONDS )); then
    printf 'Timeout aguardando SonarQube iniciar.\n' >&2
    exit 1
  fi

  sleep 5
  elapsed=$((elapsed + 5))
done

printf 'Executando analise SonarQube...\n'
./mvnw clean verify sonar:sonar \
  -Dsonar.host.url="$SONAR_HOST_URL" \
  -Dsonar.token="$SONAR_TOKEN" \
  -Dsonar.qualitygate.wait=true
