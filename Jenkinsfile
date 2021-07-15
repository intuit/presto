@Library('ibp-libraries')
def jenkinsRole = 'arn:aws:iam::000000000000:role/tmp-presto-jenkins'

pipeline {
  agent {
    kubernetes {
            label "presto-${UUID.randomUUID().toString()}"
            defaultContainer "presto-oss"
            yaml """
            apiVersion: v1
            kind: Pod
            metadata:
                annotations:
                    iam.amazonaws.com/role: ${jenkinsRole}
            spec:
                containers:
                - name: presto-oss
                  image: 'docker.intuit.com/data/kgpt/curation/service/jenkins-toolbox:latest'
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
	  container('presto-oss') {
            sh '''
	    java -version
	    mvn -v
	    '''
          }
      }
    }

    stage('Build') {
      steps {
        echo 'building Maven Package'
          container('presto-oss') {
	    sh '''
 	    cd presto-spark-launcher
	    ls -lstr
	    '''
          }
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
