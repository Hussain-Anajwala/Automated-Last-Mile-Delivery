# LastMile Ops application image.
#
# Packages the JAR that the pipeline has already built and tested (build once, promote the same
# artifact), so run `mvnw package` (or the pipeline) first. Two stages: the first splits the Spring
# Boot JAR into layers so dependency layers are cached between releases; the second is a small JRE
# image running as an unprivileged user. No secrets or environment-specific configuration are baked
# in: database settings, profile and passwords are injected at runtime.
FROM eclipse-temurin:21.0.12.1_1-jre-alpine-3.24 AS layers
WORKDIR /layers
COPY target/lastmile-delivery.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted

FROM eclipse-temurin:21.0.12.1_1-jre-alpine-3.24
ARG GIT_COMMIT=unknown
ARG VERSION=dev
LABEL org.opencontainers.image.title="lastmile-delivery" \
      org.opencontainers.image.description="Automated Last-Mile Delivery Management Platform" \
      org.opencontainers.image.source="https://github.com/Hussain-Anajwala/Automated-Last-Mile-Delivery" \
      org.opencontainers.image.revision="${GIT_COMMIT}" \
      org.opencontainers.image.version="${VERSION}"

RUN addgroup -S -g 10001 app && adduser -S -D -H -u 10001 -G app app
WORKDIR /app
COPY --from=layers --chown=10001:10001 /layers/extracted/dependencies/ ./
COPY --from=layers --chown=10001:10001 /layers/extracted/spring-boot-loader/ ./
COPY --from=layers --chown=10001:10001 /layers/extracted/snapshot-dependencies/ ./
COPY --from=layers --chown=10001:10001 /layers/extracted/application/ ./

USER 10001:10001
ENV LASTMILE_SERVER_PORT=8080 \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError -Djava.io.tmpdir=/tmp"
EXPOSE 8080

# Readiness (includes the database), so "healthy" never means "process up but unusable".
HEALTHCHECK --interval=10s --timeout=3s --start-period=60s --retries=6 \
  CMD wget -q -O /dev/null http://127.0.0.1:8080/api/v1/health/ready || exit 1

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
