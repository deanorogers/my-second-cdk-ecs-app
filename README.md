# Quarkus accounts service

This project is now a simple Java Quarkus application with an in-memory `Account` API.

## Endpoints

- `GET /` returns a plain-text status message.
- `GET /accounts` returns the current list of accounts.
- `GET /accounts/{id}` returns a single account or `404`.
- `POST /accounts` creates an account from JSON and returns `201 Created`.

The service starts with these hard-coded accounts:

- `1001` / `Checking`
- `2001` / `Savings`
- `3001` / `Emergency Fund`

Set `TRANSACTIONS_BASE_URL` to the transactions service base URL, including its
scheme and port if needed. It defaults to `http://localhost:8081`; omit a
trailing slash.

## Run locally in dev mode

```bash
./mvnw quarkus:dev
```

## Test

```bash
./mvnw test
```

## Package

```bash
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

## Build the Docker image

```bash
docker build -t quarkus-accounts:local .
```

## Run the Docker image

The container runs Quarkus on port `80` in the packaged profile, so map host port `8080` to container port `80`:

```bash
docker run --rm -p 8080:80 --name quarkus-accounts quarkus-accounts:local
```

## Verify

```bash
curl -s http://localhost:8080/
curl -s http://localhost:8080/accounts | jq
curl -s -X POST http://localhost:8080/accounts \
  -H 'Content-Type: application/json' \
  -d '{"name":"Vacation Fund","currency":"USD","balance":750.25}' | jq
```

Or run the smoke test script:

```bash
bash scripts/smoke-test.sh
```

## Deploy to ECS

The script `scripts/deploy-ecs.sh` performs:

1. Docker build
2. Docker push to ECR with immutable sequential tag format `000001-<imagehash12>`
3. ECS task definition registration with the new image tag
4. ECS service update with force new deployment

Required environment variables:

- `AWS_REGION`
- `AWS_ACCOUNT_ID`
- `ECR_REPOSITORY`
- `ECS_CLUSTER`
- `ECS_SERVICE`
- `TASK_DEFINITION_FAMILY`

Optional environment variables:

- `CONTAINER_NAME` (default: first container in current task definition)
- `DOCKERFILE_PATH` (default: `Dockerfile`)
- `CONTEXT_DIR` (default: `.`)
- `DOCKER_PLATFORM` (default: `linux/amd64`, useful on Apple Silicon for ECS x86_64)

Run:

```bash
AWS_REGION=us-east-1 \
AWS_ACCOUNT_ID=123456789012 \
ECR_REPOSITORY=quarkus-accounts \
ECS_CLUSTER=my-ecs-cluster \
ECS_SERVICE=my-ecs-service \
TASK_DEFINITION_FAMILY=my-task-family \
bash scripts/deploy-ecs.sh
```

### Use an environment file

```bash
cp .env.deploy.example .env.deploy.local
bash scripts/deploy-ecs.sh --env-file .env.deploy.local
```

Notes:

- `.env.deploy.local` is ignored by `.gitignore`
- `.env.deploy.example` is safe to commit as a template

