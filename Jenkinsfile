// LastMile Ops pipeline (pipeline as code). docs/CI_JENKINS.md describes every stage.
//
// Quality gates in order: unit/integration tests, then Selenium journeys. Deployment runs only for branches listed in DEPLOY_BRANCHES and only after every earlier stage
// has passed: a failed build or test can never reach the deployment stage.
def DEPLOY_BRANCHES = ['main', 'develop', 'ci/jenkins-pipeline']

pipeline {
    agent any

    parameters {
        choice(name: 'DEPLOY_PROFILE', choices: ['demo', 'prod'],
               description: 'Spring profile of the deployed app. "demo" seeds fictional accounts and deliveries.')
        string(name: 'HTTP_PORT', defaultValue: '8088', trim: true,
               description: 'Host port for Nginx, bound to 127.0.0.1 only (1024-65535).')
        booleanParam(name: 'DEPLOY', defaultValue: true,
                     description: 'Deploy after a successful build (deployment branches only).')
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '20'))
        disableConcurrentBuilds()
        timeout(time: 45, unit: 'MINUTES')
    }

    environment {
        COMPOSE_PROJECT_NAME = 'lastmile'
        APP_IMAGE = 'lastmile-delivery'
        NGINX_IMAGE = 'lastmile-nginx'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.GIT_SHORT = sh(script: 'git rev-parse --short=12 HEAD', returnStdout: true).trim()
                    env.IMAGE_TAG = env.GIT_SHORT
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
                    echo "Parameters: DEPLOY_PROFILE=${params.DEPLOY_PROFILE}, HTTP_PORT=${params.HTTP_PORT}, DEPLOY=${params.DEPLOY}"
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
                    ./mvnw -B -ntp verify -Pselenium                         -Dtest=NoSuchTest -Dsurefire.failIfNoSpecifiedTests=false                         -Dselenium.chrome.binary=/usr/bin/chromium                         -Dselenium.chromedriver=/usr/bin/chromedriver                         -Dselenium.noSandbox=true
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
                    docker build -q -t "$APP_IMAGE:$IMAGE_TAG" \
                        --build-arg GIT_COMMIT="$GIT_COMMIT" --build-arg VERSION="$IMAGE_TAG" .
                    docker build -q -t "$NGINX_IMAGE:$IMAGE_TAG" --build-arg GIT_COMMIT="$GIT_COMMIT" deploy/nginx
                    docker image inspect "$APP_IMAGE:$IMAGE_TAG" \
                        --format 'app image {{.Id}} user={{.Config.User}} revision={{index .Config.Labels "org.opencontainers.image.revision"}}'
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
                withCredentials([
                    string(credentialsId: 'lastmile-db-password', variable: 'LASTMILE_DB_PASSWORD'),
                    string(credentialsId: 'lastmile-demo-password', variable: 'LASTMILE_DEMO_PASSWORD')
                ]) {
                    withEnv([
                        "LASTMILE_IMAGE=${env.APP_IMAGE}",
                        "LASTMILE_NGINX_IMAGE=${env.NGINX_IMAGE}",
                        "LASTMILE_IMAGE_TAG=${env.IMAGE_TAG}",
                        "LASTMILE_HTTP_PORT=${params.HTTP_PORT}",
                        "SPRING_PROFILES_ACTIVE=${params.DEPLOY_PROFILE}"
                    ]) {
                        // --wait returns non-zero unless every service reaches running/healthy in time.
                        sh 'docker compose -f deploy/compose.yaml up -d --remove-orphans --wait --wait-timeout 180'
                    }
                }
            }
        }

        stage('Verify deployment') {
            when {
                allOf {
                    expression { params.DEPLOY }
                    expression { DEPLOY_BRANCHES.contains(env.BRANCH_NAME) }
                }
            }
            steps {
                // Checks the full path Nginx -> app -> PostgreSQL from inside the Nginx container
                // (the published port is bound to the Windows host's 127.0.0.1, not reachable from Jenkins).
                sh '''
                    docker compose -p "$COMPOSE_PROJECT_NAME" ps --format 'table {{.Service}}	{{.Image}}	{{.Status}}'
                    ready=$(docker exec "$COMPOSE_PROJECT_NAME-nginx-1" wget -q -O - http://127.0.0.1/api/v1/health/ready)
                    echo "readiness via nginx: $ready"
                    echo "$ready" | grep -q '"status":"UP"'
                    deployed=$(docker inspect --format '{{index .Config.Labels "org.opencontainers.image.revision"}}' "$COMPOSE_PROJECT_NAME-app-1")
                    echo "deployed app revision: $deployed"
                    test "$deployed" = "$GIT_COMMIT"
                '''
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
        failure {
            script {
                if (params.DEPLOY && DEPLOY_BRANCHES.contains(env.BRANCH_NAME)) {
                    // Diagnostics only; logs never contain the injected secrets.
                    sh 'docker compose -p "$COMPOSE_PROJECT_NAME" ps || true'
                    sh 'docker compose -p "$COMPOSE_PROJECT_NAME" logs --no-color --tail 80 app || true'
                }
            }
        }
    }
}
