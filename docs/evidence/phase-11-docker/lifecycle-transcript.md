# Phase 11 — Docker lifecycle transcript (2026-10-19)

Produced by `bash scripts/docker-lifecycle-demo.sh` (exit 0) on Docker Desktop 29.8.2. The throwaway
database password was generated inside the script and is shown as `***`.

```text
== 1. Build and tag (commit 887f5e074412, version 0.1.0-SNAPSHOT)

$ docker build -q -t lastmile-delivery:887f5e074412 --build-arg GIT_COMMIT=887f5e07441202f986e0bc7f3bd090bc1a4befec --build-arg VERSION=0.1.0-SNAPSHOT .
sha256:435da907d9bcf3e5e8a3412790b0fbc8ec95fa57baf129586256bdb1a0c20bb9

$ docker tag lastmile-delivery:887f5e074412 lastmile-delivery:0.1.0-SNAPSHOT-887f5e074412

$ docker image ls lastmile-delivery --format table {{.Repository}}\t{{.Tag}}\t{{.ID}}\t{{.Size}}
REPOSITORY          TAG                           IMAGE ID       SIZE
lastmile-delivery   0.1.0-SNAPSHOT-887f5e074412   435da907d9bc   405MB
lastmile-delivery   887f5e074412                  435da907d9bc   405MB
lastmile-delivery   eb87044cb837                  160d7b9f2768   406MB
lastmile-delivery   62d40a36b414                  5667d227f035   406MB
lastmile-delivery   3af9df95d0b1                  898152c9f82d   406MB
lastmile-delivery   local-check                   bf28d7adb2c8   405MB

$ docker history lastmile-delivery:887f5e074412 --format table {{.CreatedBy}}\t{{.Size}} --no-trunc=false
CREATED BY                                      SIZE
ENTRYPOINT ["java" "org.springframework.boot…   0B
HEALTHCHECK {Test:[CMD-SHELL wget -q -O /dev…   0B
EXPOSE [8080/tcp]                               0B
ENV LASTMILE_SERVER_PORT=8080 JAVA_TOOL_OPTI…   0B
USER 10001:10001                                0B
COPY --chown=10001:10001 /layers/extracted/a…   483kB
COPY --chown=10001:10001 /layers/extracted/s…   4.1kB
COPY --chown=10001:10001 /layers/extracted/s…   696kB
COPY --chown=10001:10001 /layers/extracted/d…   61.9MB
WORKDIR /app                                    8.19kB
RUN |2 GIT_COMMIT=887f5e07441202f986e0bc7f3b…   32.8kB
LABEL org.opencontainers.image.title=lastmil…   0B
ARG VERSION=0.1.0-SNAPSHOT                      0B
ARG GIT_COMMIT=887f5e07441202f986e0bc7f3bd09…   0B
ENTRYPOINT ["/__cacert_entrypoint.sh"]          0B
COPY --chmod=755 entrypoint.sh /__cacert_ent…   12.3kB
RUN /bin/sh -c set -eux;     echo "Verifying…   12.3kB
RUN /bin/sh -c set -eux;     ARCH="$(apk --p…   165MB
ENV JAVA_VERSION=jdk-21.0.12.1+1                0B
RUN /bin/sh -c set -eux;     apk add --no-ca…   37.9MB
ENV LANG=en_US.UTF-8 LANGUAGE=en_US:en LC_AL…   0B
ENV PATH=/opt/java/openjdk/bin:/usr/local/sb…   0B
ENV JAVA_HOME=/opt/java/openjdk                 0B
CMD ["/bin/sh"]                                 0B
ADD alpine-minirootfs-3.24.2-x86_64.tar.gz /…   9.08MB

== 2. Run with runtime configuration (no secrets in the image)

$ docker network create lm-lifecycle
ee15452eefbb5e9fc088af35303dab986867008e9d1973fcc4d40ae14839347f

$ docker run -d --name lm-lifecycle-db --network lm-lifecycle -e POSTGRES_DB=lastmile -e POSTGRES_USER=lastmile -e POSTGRES_PASSWORD=*** postgres:17.11-alpine3.24
735e615f9360183bfcd217bc0fafd69cc25d27dc03d9f74be080547ebc949070

$ sh -c until docker exec lm-lifecycle-db pg_isready -U lastmile -d lastmile >/dev/null 2>&1; do sleep 1; done; echo db ready
db ready

$ docker run -d --name lm-lifecycle-app --network lm-lifecycle -p 127.0.0.1:18081:8080 -e LASTMILE_DB_URL=jdbc:postgresql://lm-lifecycle-db:5432/lastmile -e LASTMILE_DB_USER=lastmile -e LASTMILE_DB_PASSWORD=*** --read-only --tmpfs /tmp --cap-drop ALL --security-opt no-new-privileges:true lastmile-delivery:887f5e074412
c5ee5cc2ebd02c07ea231dffda55c6f47dad0ce1d8c6d76cf8e057bb93cead22

$ sh -c for i in $(seq 1 60); do s=$(docker inspect -f "{{.State.Health.Status}}" lm-lifecycle-app); [ "$s" = healthy ] && break; sleep 2; done; echo "health: $s"
health: healthy

$ curl -s -w  HTTP %{http_code}\n http://127.0.0.1:18081/api/v1/health/ready
{"status":"UP"} HTTP 200

== 3. Inspect

$ docker inspect lm-lifecycle-app --format user={{.Config.User}} readonly={{.HostConfig.ReadonlyRootfs}} capdrop={{.HostConfig.CapDrop}} ports={{json .NetworkSettings.Ports}} revision={{index .Config.Labels "org.opencontainers.image.revision"}}
user=10001:10001 readonly=true capdrop=[ALL] ports={"8080/tcp":[{"HostIp":"127.0.0.1","HostPort":"18081"}]} revision=887f5e07441202f986e0bc7f3bd090bc1a4befec

$ docker exec lm-lifecycle-app id
uid=10001(app) gid=10001(app) groups=10001(app)

$ docker ps --filter name=lm-lifecycle --format table {{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}
NAMES              IMAGE                            STATUS                    PORTS
lm-lifecycle-app   lastmile-delivery:887f5e074412   Up 24 seconds (healthy)   127.0.0.1:18081->8080/tcp
lm-lifecycle-db    postgres:17.11-alpine3.24        Up 33 seconds             5432/tcp

== 4. Logs

$ sh -c docker logs lm-lifecycle-app 2>&1 | grep -E "Started LastMileApplication|Successfully applied|Tomcat started" | cut -c1-160
2026-10-18T22:41:49.904Z  INFO 1 --- [lastmile-delivery] [           main] o.f.core.internal.command.DbMigrate      : Successfully applied 1 migration to schema
2026-10-18T22:41:55.892Z  INFO 1 --- [lastmile-delivery] [           main] o.s.boot.tomcat.TomcatWebServer          : Tomcat started on port 8080 (http) with co
2026-10-18T22:41:55.912Z  INFO 1 --- [lastmile-delivery] [           main] com.lastmile.LastMileApplication         : Started LastMileApplication in 12.42 secon

== 5. Stop / start / restart

$ docker stop lm-lifecycle-app
lm-lifecycle-app

$ docker ps -a --filter name=lm-lifecycle-app --format {{.Names}} {{.Status}}
lm-lifecycle-app Exited (143) 2 seconds ago

$ docker start lm-lifecycle-app
lm-lifecycle-app

$ sh -c for i in $(seq 1 60); do s=$(docker inspect -f "{{.State.Health.Status}}" lm-lifecycle-app); [ "$s" = healthy ] && break; sleep 2; done; echo "health after start: $s"
health after start: healthy

$ docker restart lm-lifecycle-app
lm-lifecycle-app

$ sh -c for i in $(seq 1 60); do s=$(docker inspect -f "{{.State.Health.Status}}" lm-lifecycle-app); [ "$s" = healthy ] && break; sleep 2; done; echo "health after restart: $s"
health after restart: healthy

$ curl -s -w  HTTP %{http_code}\n http://127.0.0.1:18081/api/v1/health/ready
{"status":"UP"} HTTP 200

== 6. Readiness reflects the database

$ docker stop lm-lifecycle-db
lm-lifecycle-db

$ curl -s -w  HTTP %{http_code}\n http://127.0.0.1:18081/api/v1/health/ready
{"status":"DOWN"} HTTP 503

$ curl -s -w  HTTP %{http_code}\n http://127.0.0.1:18081/api/v1/health/live
{"status":"UP"} HTTP 200

$ docker start lm-lifecycle-db
lm-lifecycle-db

== 7. Remove (only this demo's resources)

$ docker rm -f lm-lifecycle-app lm-lifecycle-db
lm-lifecycle-app
lm-lifecycle-db

$ docker network rm lm-lifecycle
lm-lifecycle

$ docker ps -a --filter name=lm-lifecycle --format {{.Names}}
(no containers listed = removed)
```
