pipeline {
  agent {
    docker {
      image 'maven:3.9.5-eclipse-temurin-21'
    }
  }

  options {
    buildDiscarder(logRotator(numToKeepStr: '10'))
  }

  triggers {
    cron('H 5,13 * * 1-5')
  }

  stages {
    stage('test') {
      steps {
        script {
          maven cmd: '-Duser.home=/tmp/maven clean test -Dmaven.test.failure.ignore=true'
        }
        recordIssues tools: [java()], unstableTotalAll: 1
        recordIssues tools: [mavenConsole()], unstableTotalAll: 1
        junit 'target/surefire-reports/**/*.xml' 
      }      
    }
  }
}
