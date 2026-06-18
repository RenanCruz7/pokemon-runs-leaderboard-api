.PHONY: sonar sonar-up sonar-down

sonar:
	./scripts/run-sonarqube-analysis.sh

sonar-up:
	docker compose up -d sonarqube-db sonarqube

sonar-down:
	./scripts/stop-sonarqube.sh
