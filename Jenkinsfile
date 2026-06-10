pipeline {
    agent { label 'aws-T2XLarge' }
    tools {
        jdk 'jdk-11'
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
                withCredentials([usernamePassword(
                        credentialsId: 'tt-nexus-credentials',
                        usernameVariable: 'NEXUS_USER',
                        passwordVariable: 'NEXUS_PASS')]) {
                    sh '''
                      ./gradlew --no-daemon \
                        -PrepoUser=$NEXUS_USER \
                        -PrepoPassword=$NEXUS_PASS \
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
                        :spring-webmvc:compileJava \
                        :spring-webflux:compileJava
                    '''
                }
            }
        }
        stage('Test CVE-affected modules') {
            steps {
                withCredentials([usernamePassword(
                        credentialsId: 'tt-nexus-credentials',
                        usernameVariable: 'NEXUS_USER',
                        passwordVariable: 'NEXUS_PASS')]) {
                    sh '''
                      ./gradlew --no-daemon \
                        -PrepoUser=$NEXUS_USER \
                        -PrepoPassword=$NEXUS_PASS \
                        -Dorg.gradle.java.installations.fromEnv=JAVA_HOME \
                        -Dorg.gradle.java.installations.auto-detect=false \
                        -Dorg.gradle.java.installations.auto-download=false \
                        :spring-core:test \
                        :spring-expression:test \
                        :spring-jms:test \
                        :spring-web:test \
                        :spring-webmvc:test \
                        :spring-webflux:test
                    '''
                }
            }
        }
    }
    post {
        always {
            junit testResults: '**/build/test-results/test/*.xml', allowEmptyResults: true
        }
    }
}
