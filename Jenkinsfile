pipeline {
  agent {
    docker {
      image 'maven:3.5.3-jdk-10'
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
