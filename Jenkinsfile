@Library('ibp-libraries')
def jenkinsRole = 'arn:aws:iam::000000000000:role/tmp-presto-jenkins'

pipeline {
  agent {
    kubernetes {
            label "superglue-${UUID.randomUUID().toString()}"
            defaultContainer "sbt"
            yaml """
            apiVersion: v1
            kind: Pod
            metadata:
                annotations:
                    iam.amazonaws.com/role: ${jenkinsRole}
            spec:
                containers:
                - name: sbt
                  image: 'docker.intuit.com/oicp/standard/java/amzn-corretto-jdk11'
                  command:
                  - cat
                  tty: true
                  volumeMounts:
                  - name: docker-volume
                    mountPath: /var/run/docker.sock
                volumes:
                - name: docker-volume
                  hostPath:
                    path: /var/run/dind/docker.sock
            """
        }
  }
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
    environment = 'test'
  }
}
