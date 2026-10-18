#!/usr/bin/env bash
# Phase 11 demonstration of the image and container lifecycle (Git Bash or Ubuntu/WSL).
# Uses its own resources (network lm-lifecycle, containers lm-lifecycle-*) so the deployed
# "lastmile" stack is not touched, and removes them at the end.
# Prerequisite: target/lastmile-delivery.jar (./mvnw package). Prints every command it runs.
set -euo pipefail
export MSYS_NO_PATHCONV=1

SHA=$(git rev-parse --short=12 HEAD)
VERSION=$(sed -n 's:.*<version>\(0\.[0-9.]*-SNAPSHOT\)</version>.*:\1:p' pom.xml | head -1)
IMAGE=lastmile-delivery
DB_PASSWORD=$(head -c 18 /dev/urandom | base64 | tr -dc 'A-Za-z0-9' | head -c 24)   # throwaway, never printed

run() { echo; echo "\$ $*"; "$@"; }

echo "== 1. Build and tag (commit $SHA, version $VERSION)"
run docker build -q -t "$IMAGE:$SHA" --build-arg GIT_COMMIT="$(git rev-parse HEAD)" --build-arg VERSION="$VERSION" .
run docker tag "$IMAGE:$SHA" "$IMAGE:$VERSION-$SHA"
run docker image ls "$IMAGE" --format 'table {{.Repository}}\t{{.Tag}}\t{{.ID}}\t{{.Size}}'
run docker history "$IMAGE:$SHA" --format 'table {{.CreatedBy}}\t{{.Size}}' --no-trunc=false

echo; echo "== 2. Run with runtime configuration (no secrets in the image)"
run docker network create lm-lifecycle
echo; echo '$ docker run -d --name lm-lifecycle-db --network lm-lifecycle -e POSTGRES_DB=lastmile -e POSTGRES_USER=lastmile -e POSTGRES_PASSWORD=*** postgres:17.11-alpine3.24'
docker run -d --name lm-lifecycle-db --network lm-lifecycle \
  -e POSTGRES_DB=lastmile -e POSTGRES_USER=lastmile -e POSTGRES_PASSWORD="$DB_PASSWORD" postgres:17.11-alpine3.24
run sh -c 'until docker exec lm-lifecycle-db pg_isready -U lastmile -d lastmile >/dev/null 2>&1; do sleep 1; done; echo db ready'
echo; echo "\$ docker run -d --name lm-lifecycle-app --network lm-lifecycle -p 127.0.0.1:18081:8080 -e LASTMILE_DB_URL=jdbc:postgresql://lm-lifecycle-db:5432/lastmile -e LASTMILE_DB_USER=lastmile -e LASTMILE_DB_PASSWORD=*** --read-only --tmpfs /tmp --cap-drop ALL --security-opt no-new-privileges:true $IMAGE:$SHA"
docker run -d --name lm-lifecycle-app --network lm-lifecycle -p 127.0.0.1:18081:8080 \
  -e LASTMILE_DB_URL=jdbc:postgresql://lm-lifecycle-db:5432/lastmile -e LASTMILE_DB_USER=lastmile \
  -e LASTMILE_DB_PASSWORD="$DB_PASSWORD" --read-only --tmpfs /tmp --cap-drop ALL \
  --security-opt no-new-privileges:true "$IMAGE:$SHA"
run sh -c 'for i in $(seq 1 60); do s=$(docker inspect -f "{{.State.Health.Status}}" lm-lifecycle-app); [ "$s" = healthy ] && break; sleep 2; done; echo "health: $s"'
run curl -s -w ' HTTP %{http_code}\n' http://127.0.0.1:18081/api/v1/health/ready

echo; echo "== 3. Inspect"
run docker inspect lm-lifecycle-app --format 'user={{.Config.User}} readonly={{.HostConfig.ReadonlyRootfs}} capdrop={{.HostConfig.CapDrop}} ports={{json .NetworkSettings.Ports}} revision={{index .Config.Labels "org.opencontainers.image.revision"}}'
run docker exec lm-lifecycle-app id
run docker ps --filter name=lm-lifecycle --format 'table {{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}'

echo; echo "== 4. Logs"
run sh -c 'docker logs lm-lifecycle-app 2>&1 | grep -E "Started LastMileApplication|Successfully applied|Tomcat started" | cut -c1-160'

echo; echo "== 5. Stop / start / restart"
run docker stop lm-lifecycle-app
run docker ps -a --filter name=lm-lifecycle-app --format '{{.Names}} {{.Status}}'
run docker start lm-lifecycle-app
run sh -c 'for i in $(seq 1 60); do s=$(docker inspect -f "{{.State.Health.Status}}" lm-lifecycle-app); [ "$s" = healthy ] && break; sleep 2; done; echo "health after start: $s"'
run docker restart lm-lifecycle-app
run sh -c 'for i in $(seq 1 60); do s=$(docker inspect -f "{{.State.Health.Status}}" lm-lifecycle-app); [ "$s" = healthy ] && break; sleep 2; done; echo "health after restart: $s"'
run curl -s -w ' HTTP %{http_code}\n' http://127.0.0.1:18081/api/v1/health/ready

echo; echo "== 6. Readiness reflects the database"
run docker stop lm-lifecycle-db
run curl -s -w ' HTTP %{http_code}\n' http://127.0.0.1:18081/api/v1/health/ready
run curl -s -w ' HTTP %{http_code}\n' http://127.0.0.1:18081/api/v1/health/live
run docker start lm-lifecycle-db

echo; echo "== 7. Remove (only this demo's resources)"
run docker rm -f lm-lifecycle-app lm-lifecycle-db
run docker network rm lm-lifecycle
run docker ps -a --filter name=lm-lifecycle --format '{{.Names}}'
echo "(no containers listed = removed)"
