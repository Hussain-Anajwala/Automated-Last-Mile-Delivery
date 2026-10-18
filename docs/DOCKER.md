# Docker Image and Container Lifecycle (Teacher Task 11)

## Images

| Image | Built from | Tag | Notes |
|---|---|---|---|
| `lastmile-delivery` | `Dockerfile` | 12-char commit SHA (also `<version>-<sha>` in the demo) | Layered Spring Boot JAR on `eclipse-temurin:21.0.12.1_1-jre-alpine-3.24`; user `10001:10001`; `HEALTHCHECK` on readiness; OCI labels incl. `revision` |
| `lastmile-nginx` | `deploy/nginx/Dockerfile` | same SHA as the app | `nginx:1.30.6-alpine` + baked proxy config; `HEALTHCHECK` on `/nginx-health` |

Design choices:

- **Build once, promote:** the Dockerfile packages `target/lastmile-delivery.jar`, the artifact that
  already passed unit, integration and Selenium tests in the same pipeline run.
- **Two stages:** the first stage splits the JAR into Spring Boot layers; the runtime stage copies
  dependencies (61.9 MB) separately from application classes (~0.5 MB), so a code change rebuilds
  and pushes only a small layer.
- **Smaller attack surface:** JRE (not JDK), Alpine base, non-root user, no shell scripts of our
  own, `.dockerignore` allow-list (only the JAR enters the build context).
- **No secrets or environment config in the image:** DB URL, user, password, profile and demo
  password are runtime environment variables. Build args carry only the commit and version.
- **Runtime hardening (Compose and demo):** read-only root filesystem, `tmpfs /tmp`, all Linux
  capabilities dropped, `no-new-privileges`, memory limit.

Size: `docker image ls` reports ~405 MB on disk (uncompressed); `docker history` shows the base JRE
layers (~212 MB) and the application layers (~63 MB).

## Lifecycle commands (Git Bash / Ubuntu)

```bash
./mvnw -B package -DskipTests                                     # or the pipeline's tested JAR
docker build -t lastmile-delivery:$(git rev-parse --short=12 HEAD) \
  --build-arg GIT_COMMIT=$(git rev-parse HEAD) --build-arg VERSION=0.1.0-SNAPSHOT .
docker image ls lastmile-delivery
docker history lastmile-delivery:<tag>
docker run -d --name lm-app --network <net> -p 127.0.0.1:18081:8080 \
  -e LASTMILE_DB_URL=jdbc:postgresql://<db-host>:5432/lastmile -e LASTMILE_DB_USER=lastmile \
  -e LASTMILE_DB_PASSWORD=<secret> --read-only --tmpfs /tmp --cap-drop ALL \
  --security-opt no-new-privileges:true lastmile-delivery:<tag>
docker inspect lm-app --format '{{.State.Health.Status}} {{.Config.User}}'
docker logs lm-app
docker stop lm-app && docker start lm-app && docker restart lm-app
docker rm -f lm-app
```

`scripts/docker-lifecycle-demo.sh` runs this whole sequence against a throwaway database and network
and cleans up afterwards. A real transcript is in `docs/evidence/phase-11-docker/`.

## Ports

| Container port | Published | Purpose |
|---|---|---|
| app 8080 | no (Compose); `127.0.0.1:18081` in the lifecycle demo only | HTTP |
| nginx 80 | `127.0.0.1:${LASTMILE_HTTP_PORT:-8088}` | single entry point |
| db 5432 | no | PostgreSQL on the internal `backend` network |
