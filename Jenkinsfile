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
	    echo 'convert files to unix format to avoid style errors'
            sh '''
            find . -type f -print0 | xargs -0 -n 1 -P 4 dos2unix
	    '''
            echo 'compile the entire project with root pom.xml maven file'
            sh '''
	    mvn clean install -DskipTests
	    '''
          }
      }
    }

    stage('Unit Test') {
      steps {
        echo 'Unit Testing'
	sh '''
	ls -lstr
	ls -lstr target/
	'''
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
