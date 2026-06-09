pipeline {
    agent any
    options {
        timeout(time: 2, unit: 'HOURS')
        timestamps()
        buildDiscarder(logRotator(daysToKeepStr: '90', numToKeepStr: '20'))
    }
    stages {
        stage('Compile CVE-affected modules') {
            steps {
                sh '''
                  ./gradlew --no-daemon \\
                    :spring-core:compileJava \\
                    :spring-beans:compileJava \\
                    :spring-aop:compileJava \\
                    :spring-context:compileJava \\
                    :spring-tx:compileJava \\
                    :spring-expression:compileJava \\
                    :spring-jms:compileJava \\
                    :spring-web:compileJava \\
                    :spring-webmvc:compileJava \\
                    :spring-webflux:compileJava
                '''
            }
        }
        stage('Test CVE-affected modules') {
            steps {
                sh '''
                  ./gradlew --no-daemon \\
                    :spring-core:test \\
                    :spring-expression:test \\
                    :spring-jms:test \\
                    :spring-web:test \\
                    :spring-webmvc:test \\
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
