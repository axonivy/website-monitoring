pipeline {
  agent {
    docker {
      image 'maven:3.5.2-jdk-8'
    }
  }
  triggers {
    cron '@midnight'
  }
  stages {
    stage('test') {
      steps {
        script {
          maven cmd: 'clean test -Dmaven.test.failure.ignore=true'
        }
      }
      post {
        success {
          junit 'target/surefire-reports/**/*.xml' 
        }
      }
    }
  }
}
