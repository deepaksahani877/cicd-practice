pipeline {

    agent any

    stages {

        stage('Clone') {
            steps {
                git 'https://github.com/deepaksahani877/cicd-practice.git'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package'
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t springboot-demo .'
            }
        }

        stage('Run Container') {
            steps {
                sh 'docker run -d -p 8080:8080 springboot-demo'
            }
        }

    }

}