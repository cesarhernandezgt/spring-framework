pipeline {
    agent any
    tools {
        jdk 'jdk-17'
        // Gradle tool not pinned — repo's gradle-wrapper downloads its own.
        // If wrapper download is sandboxed, uncomment:
        // gradle 'gradle-9.6'
    }
    options {
        timeout(time: 2, unit: 'HOURS')
        timestamps()
        buildDiscarder(logRotator(daysToKeepStr: '90', numToKeepStr: '20'))
    }
    stages {
        stage('Show toolchain') {
            steps {
                sh '''
                  echo "JAVA_HOME=$JAVA_HOME"
                  java -version
                  $JAVA_HOME/bin/javac -version
                '''
            }
        }
        stage('Compile CVE-affected modules') {
            steps {
                sh '''
                  ./gradlew --no-daemon \
                    -Dorg.gradle.java.installations.fromEnv=JAVA_HOME \
                    -Dorg.gradle.java.installations.auto-detect=false \
                    -Dorg.gradle.java.installations.auto-download=false \
                    :spring-core:compileJava \
                    :spring-beans:compileJava \
                    :spring-aop:compileJava \
                    :spring-context:compileJava \
                    :spring-tx:compileJava \
                    :spring-expression:compileJava \
                    :spring-jms:compileJava \
                    :spring-web:compileJava \
                    :spring-webmvc:compileJava\\
                    :spring-webflux:compileJava
                '''
            }
        }
        stage('Test CVE-affected modules') {
            steps {
                sh '''
                  ./gradlew --no-daemon \
                    -Dorg.gradle.java.installations.fromEnv=JAVA_HOME \
                    -Dorg.gradle.java.installations.auto-detect=false \
                    -Dorg.gradle.java.installations.auto-download=false \
                    :spring-core:test \
                    :spring-expression:test \
                    :spring-jms:test \
                    :spring-web:test \
                    :spring-webmvc:test\\
                    :spring-webflux:test
                '''
            }
        }
    }
    post {
        always {
            junit testResults: '**/build/test-results/test/*.xml', allowEmptyResults: true
        }
    }
}
