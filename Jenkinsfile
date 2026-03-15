pipeline {
    agent any

    options {
        skipDefaultCheckout(true)
    }

    environment {
        APP_NAME = 'cicdpractice'
        K8S_NAMESPACE = 'default'
        IMAGE_TAG = "${BUILD_NUMBER}"
    }

    stages {
        stage('Preflight') {
            steps {
                script {
                    if (isUnix()) {
                        sh '''
                            set -e
                            which git && git --version
                            which mvn && mvn -version
                            which docker && docker --version
                            which kubectl && kubectl version --client
                            which minikube && minikube version
                        '''
                    } else {
                        bat '''
                            where git && git --version
                            where mvn && mvn -version
                            where docker && docker --version
                            where kubectl && kubectl version --client
                            where minikube && minikube version
                        '''
                    }
                }
            }
        }

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build and Test') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'mvn clean verify'
                    } else {
                        bat 'mvn clean verify'
                    }
                }
            }
        }

        stage('Build Docker Image (Minikube)') {
            steps {
                script {
                    if (isUnix()) {
                        sh '''
                            eval "$(minikube -p minikube docker-env)"
                            docker build -t ${APP_NAME}:${IMAGE_TAG} -t ${APP_NAME}:latest .
                            docker images | grep ${APP_NAME}
                        '''
                    } else {
                        powershell '''
                            minikube -p minikube docker-env --shell powershell | Invoke-Expression
                            docker build -t "$env:APP_NAME:$env:IMAGE_TAG" -t "$env:APP_NAME:latest" .
                            docker images | Select-String $env:APP_NAME
                        '''
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                script {
                    if (isUnix()) {
                        sh '''
                            kubectl apply -n ${K8S_NAMESPACE} -f k8s/deployment.yaml
                            kubectl apply -n ${K8S_NAMESPACE} -f k8s/service.yaml
                            kubectl set image deployment/${APP_NAME} ${APP_NAME}=${APP_NAME}:${IMAGE_TAG} -n ${K8S_NAMESPACE}
                            kubectl rollout status deployment/${APP_NAME} -n ${K8S_NAMESPACE} --timeout=180s
                        '''
                    } else {
                        powershell '''
                            kubectl apply -n $env:K8S_NAMESPACE -f k8s/deployment.yaml
                            kubectl apply -n $env:K8S_NAMESPACE -f k8s/service.yaml
                            kubectl set image deployment/$env:APP_NAME $env:APP_NAME=$env:APP_NAME:$env:IMAGE_TAG -n $env:K8S_NAMESPACE
                            kubectl rollout status deployment/$env:APP_NAME -n $env:K8S_NAMESPACE --timeout=180s
                        '''
                    }
                }
            }
        }
    }

    post {
        success {
            script {
                if (isUnix()) {
                    sh '''
                        IP="$(minikube ip)"
                        echo "Deployment successful. Access URL: http://${IP}:30007"
                    '''
                } else {
                    powershell '''
                        $ip = minikube ip
                        Write-Host "Deployment successful. Access URL: http://$ip`:30007"
                    '''
                }
            }
        }
        always {
            script {
                if (isUnix()) {
                    sh(returnStatus: true, script: 'kubectl get pods -n ${K8S_NAMESPACE} -o wide')
                } else {
                    powershell(returnStatus: true, script: 'kubectl get pods -n $env:K8S_NAMESPACE -o wide')
                }
            }
        }
    }
}
