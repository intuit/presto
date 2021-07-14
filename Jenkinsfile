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
                - name: sbt
                  image: 'docker.intuit.com/data/kgpt/curation/service/jenkins-toolbox:ce3b7d6e928bc8f9224b3d14f8f43c380aa55027'
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
        sh '''
	java -version
	'''
	sh '''
	mvn -v
	'''
      }
    }

    stage('Build') {
      steps {
        echo 'building Maven Package'
	sh '''
	cd presto-spark-launcher
	'''
	sh '''
	ls -lstr
	'''
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
