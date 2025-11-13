# Population Reports Project

[![CI](https://github.com/V8kt8r/devops/actions/workflows/ci.yml/badge.svg)](https://github.com/V8kt8r/devops/actions/workflows/ci.yml)

Java + MySQL project for the SET08103 coursework (population reports based on the `world` database).

## Technologies

- Java 17
- Maven
- MySQL 8.0 (Docker)
- JUnit 5 (unit + integration tests)
- JaCoCo (code coverage)
- GitHub Actions (CI)

## How to run locally

### 1. Start the database in Docker

From the project root:

```bash
docker compose up -d
