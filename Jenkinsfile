pipeline {
  agent {
    docker {
      image 'maven:3.5.3-jdk-10'
    }
  }
  triggers {
    cron('H 5,13 0 0 1-5')
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
