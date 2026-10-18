// LastMile Ops pipeline (Phase 7: continuous integration).
// Later phases extend this file with Selenium, image, registry and deployment stages.
pipeline {
    agent any

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '20'))
        disableConcurrentBuilds()
        timeout(time: 45, unit: 'MINUTES')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.GIT_SHORT = sh(script: 'git rev-parse --short=12 HEAD', returnStdout: true).trim()
                }
                echo "Building ${env.BRANCH_NAME} at ${env.GIT_COMMIT} (${env.GIT_SHORT})"
                sh 'java -version 2>&1 | head -1'
            }
        }

        stage('Build and unit/integration tests') {
            steps {
                sh './mvnw -B -ntp clean verify'
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
