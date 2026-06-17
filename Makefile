.PHONY: sonar-up sonar-down sonar analyze-sonar

sonar-up:
	./scripts/run-sonarqube-analysis.sh

sonar-down:
	./scripts/stop-sonarqube.sh

sonar: analyze-sonar

analyze-sonar:
	./scripts/run-sonarqube-analysis.sh
