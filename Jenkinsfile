// LastMile Ops pipeline (pipeline as code). docs/CI_JENKINS.md describes every stage.
//
// Quality gates in order: unit/integration tests, then Selenium journeys. Images are published to the
// local registry only after both pass, and deployment runs only for branches in DEPLOY_BRANCHES.
// If the deployed candidate is not healthy, the pipeline restores the release that was running
// before (last known good) and fails the build.
def DEPLOY_BRANCHES = ['main', 'develop', 'ci/jenkins-pipeline']

// Deploys the given app/nginx image references with the stack's runtime secrets.
def composeUp(String appRef, String nginxRef, String dbHost) {
    def app = appRef.lastIndexOf(':')
    def ngx = nginxRef.lastIndexOf(':')
    withCredentials([
        string(credentialsId: 'lastmile-db-password', variable: 'LASTMILE_DB_PASSWORD'),
        string(credentialsId: 'lastmile-demo-password', variable: 'LASTMILE_DEMO_PASSWORD')
    ]) {
        withEnv([
            "LASTMILE_IMAGE=${appRef.substring(0, app)}",
            "LASTMILE_IMAGE_TAG=${appRef.substring(app + 1)}",
            "LASTMILE_NGINX_IMAGE=${nginxRef.substring(0, ngx)}",
            "LASTMILE_HTTP_PORT=${params.HTTP_PORT}",
            "SPRING_PROFILES_ACTIVE=${params.DEPLOY_PROFILE}",
            "LASTMILE_DB_HOST=${dbHost}"
        ]) {
            // Registry references are pulled (proves the registry serves them); older local-only
            // tags from before the registry existed are already present on the daemon.
            if (appRef.startsWith("${env.REGISTRY}/") && nginxRef.startsWith("${env.REGISTRY}/")) {
                sh 'docker compose -f deploy/compose.yaml pull app nginx'
            }
            // --wait returns non-zero unless every service reaches running/healthy in time.
            sh 'docker compose -f deploy/compose.yaml up -d --remove-orphans --wait --wait-timeout 150'
        }
    }
}

// Readiness through Nginx (full path Nginx -> app -> PostgreSQL) and deployed revision check.
def verifyDeployment(String expectedRevision) {
    sh """
        docker compose -p "\$COMPOSE_PROJECT_NAME" ps --format 'table {{.Service}}\\t{{.Image}}\\t{{.Status}}'
        ready=\$(docker exec "\$COMPOSE_PROJECT_NAME-nginx-1" wget -q -O - http://127.0.0.1/api/v1/health/ready)
        echo "readiness via nginx: \$ready"
        echo "\$ready" | grep -q '"status":"UP"'
        deployed=\$(docker inspect --format '{{index .Config.Labels "org.opencontainers.image.revision"}}' "\$COMPOSE_PROJECT_NAME-app-1")
        echo "deployed app revision: \$deployed"
        test "\$deployed" = "${expectedRevision}"
    """
}

pipeline {
    agent any

    parameters {
        choice(name: 'DEPLOY_PROFILE', choices: ['demo', 'prod'],
               description: 'Spring profile of the deployed app. "demo" seeds fictional accounts and deliveries.')
        string(name: 'HTTP_PORT', defaultValue: '8088', trim: true,
               description: 'Host port for Nginx, bound to 127.0.0.1 only (1024-65535).')
        booleanParam(name: 'DEPLOY', defaultValue: true,
                     description: 'Deploy after a successful build (deployment branches only).')
        choice(name: 'FAULT_INJECTION', choices: ['none', 'unreachable-database'],
               description: 'Recovery drill only: deploy the candidate with a wrong database host so its health check fails and the pipeline must roll back.')
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '20'))
        disableConcurrentBuilds()
        timeout(time: 60, unit: 'MINUTES')
    }

    environment {
        COMPOSE_PROJECT_NAME = 'lastmile'
        REGISTRY = 'localhost:5000'                    // as seen by the Docker daemon
        REGISTRY_API = 'http://lastmile-registry:5000' // as seen from this Jenkins container
        APP_REPO = 'lastmile-delivery'
        NGINX_REPO = 'lastmile-nginx'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.GIT_SHORT = sh(script: 'git rev-parse --short=12 HEAD', returnStdout: true).trim()
                    env.IMAGE_TAG = env.GIT_SHORT
                    env.APP_REF = "${env.REGISTRY}/${env.APP_REPO}:${env.IMAGE_TAG}"
                    env.NGINX_REF = "${env.REGISTRY}/${env.NGINX_REPO}:${env.IMAGE_TAG}"
                }
                echo "Building ${env.BRANCH_NAME} at ${env.GIT_COMMIT} (image tag ${env.IMAGE_TAG})"
                sh 'java -version 2>&1 | head -1'
            }
        }

        stage('Validate parameters') {
            steps {
                script {
                    if (!(params.HTTP_PORT ==~ /\d{4,5}/) || params.HTTP_PORT.toInteger() < 1024 || params.HTTP_PORT.toInteger() > 65535) {
                        error("HTTP_PORT must be a number between 1024 and 65535, got '${params.HTTP_PORT}'")
                    }
                    echo "Parameters: DEPLOY_PROFILE=${params.DEPLOY_PROFILE}, HTTP_PORT=${params.HTTP_PORT}, DEPLOY=${params.DEPLOY}, FAULT_INJECTION=${params.FAULT_INJECTION}"
                }
            }
        }

        stage('Build and unit/integration tests') {
            steps {
                sh './mvnw -B -ntp clean verify'
            }
        }

        stage('Selenium quality gate') {
            steps {
                // Browser journeys against the real app (random port, embedded PostgreSQL) in the
                // container's Chromium. --no-sandbox: Chromium's sandbox cannot start inside this
                // unprivileged container; the browser only visits the local test application.
                sh '''
                    ./mvnw -B -ntp verify -Pselenium \
                        -Dtest=NoSuchTest -Dsurefire.failIfNoSpecifiedTests=false \
                        -Dselenium.chrome.binary=/usr/bin/chromium \
                        -Dselenium.chromedriver=/usr/bin/chromedriver \
                        -Dselenium.noSandbox=true
                '''
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/failsafe-reports/*.xml'
                    publishHTML(target: [reportName: 'Selenium report', reportDir: 'target/reports',
                                         reportFiles: 'failsafe.html', keepAll: true,
                                         alwaysLinkToLastBuild: true, allowMissing: true])
                    archiveArtifacts artifacts: 'target/selenium-artifacts/**', allowEmptyArchive: true
                }
            }
        }

        stage('Package images') {
            steps {
                sh '''
                    docker build -q -t "$APP_REF" --build-arg GIT_COMMIT="$GIT_COMMIT" --build-arg VERSION="$IMAGE_TAG" .
                    docker build -q -t "$NGINX_REF" --build-arg GIT_COMMIT="$GIT_COMMIT" deploy/nginx
                    docker image inspect "$APP_REF" \
                        --format 'app image {{.Id}} user={{.Config.User}} revision={{index .Config.Labels "org.opencontainers.image.revision"}}'
                '''
            }
        }

        stage('Publish to local registry') {
            steps {
                sh '''
                    docker push -q "$APP_REF"
                    docker push -q "$NGINX_REF"
                    for repo in "$APP_REPO" "$NGINX_REPO"; do
                        ref="$REGISTRY/$repo:$IMAGE_TAG"
                        local_digest=$(docker image inspect --format '{{range .RepoDigests}}{{println .}}{{end}}' "$ref" | grep "^$REGISTRY/$repo@" | head -1 | cut -d@ -f2)
                        registry_digest=$(curl -fsS -I \
                            -H 'Accept: application/vnd.oci.image.index.v1+json' \
                            -H 'Accept: application/vnd.docker.distribution.manifest.list.v2+json' \
                            -H 'Accept: application/vnd.oci.image.manifest.v1+json' \
                            -H 'Accept: application/vnd.docker.distribution.manifest.v2+json' \
                            "$REGISTRY_API/v2/$repo/manifests/$IMAGE_TAG" | tr -d '\\r' | awk -F': ' 'tolower($1)=="docker-content-digest"{print $2}')
                        echo "$repo:$IMAGE_TAG pushed digest=$local_digest registry digest=$registry_digest"
                        test -n "$registry_digest"
                        test "$local_digest" = "$registry_digest"
                    done
                    echo "registry tags for $APP_REPO: $(curl -fsS "$REGISTRY_API/v2/$APP_REPO/tags/list")"
                '''
            }
        }

        stage('Deploy') {
            when {
                allOf {
                    expression { params.DEPLOY }
                    expression { DEPLOY_BRANCHES.contains(env.BRANCH_NAME) }
                }
            }
            steps {
                script {
                    // Last known good = what is running now (immutable registry or local tag).
                    env.PREV_APP_REF = sh(script: 'docker inspect --format "{{.Config.Image}}" "$COMPOSE_PROJECT_NAME-app-1" 2>/dev/null || true', returnStdout: true).trim()
                    env.PREV_NGINX_REF = sh(script: 'docker inspect --format "{{.Config.Image}}" "$COMPOSE_PROJECT_NAME-nginx-1" 2>/dev/null || true', returnStdout: true).trim()
                    env.PREV_REVISION = sh(script: 'docker inspect --format "{{index .Config.Labels \\"org.opencontainers.image.revision\\"}}" "$COMPOSE_PROJECT_NAME-app-1" 2>/dev/null || true', returnStdout: true).trim()
                    echo "Last known good before deploy: app=${env.PREV_APP_REF ?: 'none'} nginx=${env.PREV_NGINX_REF ?: 'none'} revision=${env.PREV_REVISION ?: 'none'}"
                    def dbHost = params.FAULT_INJECTION == 'unreachable-database' ? 'db-does-not-exist' : 'db'
                    if (dbHost != 'db') {
                        echo "FAULT INJECTION: candidate deployed with LASTMILE_DB_HOST=${dbHost} (recovery drill)"
                    }
                    try {
                        composeUp(env.APP_REF, env.NGINX_REF, dbHost)
                        verifyDeployment(env.GIT_COMMIT)
                        echo "Deployed ${env.APP_REF} (${env.GIT_COMMIT})"
                    } catch (err) {
                        echo "Candidate ${env.APP_REF} failed deployment or health verification: ${err}"
                        sh 'docker compose -p "$COMPOSE_PROJECT_NAME" ps || true'
                        sh 'docker compose -p "$COMPOSE_PROJECT_NAME" logs --no-color --tail 40 app || true'
                        if (env.PREV_APP_REF && env.PREV_NGINX_REF && env.PREV_REVISION) {
                            echo "ROLLBACK: restoring ${env.PREV_APP_REF} / ${env.PREV_NGINX_REF}"
                            composeUp(env.PREV_APP_REF, env.PREV_NGINX_REF, 'db')
                            verifyDeployment(env.PREV_REVISION)
                            echo "ROLLBACK SUCCEEDED: ${env.PREV_APP_REF} is healthy again"
                        } else {
                            echo 'No previous release recorded; nothing to roll back to.'
                        }
                        error("Deployment of ${env.APP_REF} failed; see log above (rollback status reported).")
                    }
                }
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
        }
        success {
            archiveArtifacts artifacts: 'target/lastmile-delivery.jar', fingerprint: true
        }
    }
}
