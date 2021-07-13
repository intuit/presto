pipeline {
  agent any
  stages {
    stage('init') {
      steps {
        sh 'echo "Install dependencies"'
        sh 'echo "dependencies installed correctly"'
      }
    }

    stage('build') {
      steps {
        withMaven(globalMavenSettingsConfig: 'tmp.config', globalMavenSettingsFilePath: 'gobal-tmp.config', jdk: 'java8', maven: 'maven-123', mavenSettingsConfig: 'maven.config', mavenSettingsFilePath: '/home/user', mavenOpts: '          -Xmx2048m -Xms1024m -XX:MaxPermSize=512m -Djava.awt.headless=true', mavenLocalRepo: 'local', tempBinDir: '/tmp/bin/dir')
        readMavenPom(file: 'tmp.xml')
      }
    }

    stage('unit test') {
      steps {
        echo 'unit test'
      }
    }

    stage('integration test') {
      steps {
        echo 'integration test'
      }
    }

    stage('beta deploy') {
      steps {
        echo 'beta deploy'
      }
    }

    stage('prod deploy') {
      steps {
        echo 'prod deploy'
      }
    }

  }
}