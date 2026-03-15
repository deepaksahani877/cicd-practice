pipeline {
    agent any

    environment {
        APP_NAME = 'cicdpractice'
        K8S_NAMESPACE = 'default'
        IMAGE_TAG = "${BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build and Test') {
            steps {
                sh 'mvn clean verify'
            }
        }

        stage('Build Docker Image (Minikube)') {
            steps {
                sh '''
                    eval "$(minikube -p minikube docker-env)"
                    docker build -t ${APP_NAME}:${IMAGE_TAG} -t ${APP_NAME}:latest .
                    docker images | grep ${APP_NAME}
                '''
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh '''
                    kubectl apply -n ${K8S_NAMESPACE} -f k8s/deployment.yaml
                    kubectl apply -n ${K8S_NAMESPACE} -f k8s/service.yaml
                    kubectl set image deployment/${APP_NAME} ${APP_NAME}=${APP_NAME}:${IMAGE_TAG} -n ${K8S_NAMESPACE}
                    kubectl rollout status deployment/${APP_NAME} -n ${K8S_NAMESPACE} --timeout=180s
                '''
            }
        }
    }

    post {
        success {
            sh 'minikube service cicdpractice-service --url'
        }
        always {
            sh 'kubectl get pods -n ${K8S_NAMESPACE} -o wide'
        }
    }
}
