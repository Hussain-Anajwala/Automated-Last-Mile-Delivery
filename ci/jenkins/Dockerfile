# Local Jenkins controller for LastMile Ops (decision D10). Not for network exposure.
FROM jenkins/jenkins:2.580.1-lts-jdk21

USER root
# Docker CLI + Compose plugin from Docker's official Debian repository (builds/pushes/deploys through the
# host daemon socket), and Chromium + chromedriver from Debian for the Selenium quality gate.
RUN apt-get update \
 && apt-get install -y --no-install-recommends ca-certificates curl gnupg chromium chromium-driver fonts-liberation \
 && install -m 0755 -d /etc/apt/keyrings \
 && curl -fsSL https://download.docker.com/linux/debian/gpg -o /etc/apt/keyrings/docker.asc \
 && chmod a+r /etc/apt/keyrings/docker.asc \
 && . /etc/os-release \
 && echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/debian ${VERSION_CODENAME} stable" \
      > /etc/apt/sources.list.d/docker.list \
 && apt-get update \
 && apt-get install -y --no-install-recommends docker-ce-cli docker-compose-plugin \
 && rm -rf /var/lib/apt/lists/*

USER jenkins
COPY --chown=jenkins:jenkins plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt
COPY --chown=jenkins:jenkins casc.yaml /usr/share/jenkins/ref/casc.yaml

# Configuration as code: no setup wizard; admin credentials come from the git-ignored .env file.
ENV CASC_JENKINS_CONFIG=/usr/share/jenkins/ref/casc.yaml \
    JAVA_OPTS="-Djenkins.install.runSetupWizard=false"
