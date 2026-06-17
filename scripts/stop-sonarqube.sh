#!/usr/bin/env bash
set -euo pipefail

printf 'Parando SonarQube local...\n'
docker compose stop sonarqube sonarqube-db

printf 'Se quiser remover containers e rede do SonarQube, rode:\n'
printf 'docker compose rm -f sonarqube sonarqube-db\n'
