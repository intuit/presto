pipeline {
  agent any
  stages {
    stage('init') {
      steps {
        echo 'Installing Dependencies'
      }
    }

    stage('Build') {
      steps {
        echo 'building Maven Package'
      }
    }

    stage('Unit Test') {
      steps {
        echo 'Unit Testing'
        echo 'Integration Test'
      }
    }

    stage('Deploy Artifact') {
      steps {
        echo 'Deploy Artifact'
      }
    }

  }
  environment {
    JenkinsRole = 'arn:aws:iam::000000000000:role/tmp-role-presto'
    environment = 'test'
  }
}