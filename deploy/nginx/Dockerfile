# Nginx with the proxy configuration baked in. A bind-mounted config would not work when Jenkins
# drives the host Docker daemon from inside its container (paths resolve on the Docker host).
FROM nginx:1.30.6-alpine
ARG GIT_COMMIT=unknown
LABEL org.opencontainers.image.title="lastmile-nginx" \
      org.opencontainers.image.revision="${GIT_COMMIT}"
COPY default.conf /etc/nginx/conf.d/default.conf
HEALTHCHECK --interval=10s --timeout=3s --retries=3 \
  CMD wget -q -O /dev/null http://127.0.0.1/nginx-health || exit 1
