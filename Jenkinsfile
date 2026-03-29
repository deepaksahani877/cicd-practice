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

        stage('Cluster Ready Check') {
            steps {
                script {
                    if (isUnix()) {
                        sh '''
                            set -e
                            minikube status -p minikube
                            kubectl config use-context minikube
                            kubectl cluster-info
                        '''
                    } else {
                        powershell '''
                            $ErrorActionPreference = "Stop"
                            minikube status -p minikube
                            minikube update-context -p minikube
                            kubectl config use-context minikube
                            kubectl cluster-info
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
                            docker build -t ${APP_NAME}:${IMAGE_TAG} -t ${APP_NAME}:latest .
                            minikube image load ${APP_NAME}:${IMAGE_TAG}
                            minikube image load ${APP_NAME}:latest
                            docker images | grep ${APP_NAME} || true
                        '''
                    } else {
                        powershell '''
                            $ErrorActionPreference = "Stop"
                            $tagBuild = "$($env:APP_NAME):$($env:IMAGE_TAG)"
                            $tagLatest = "$($env:APP_NAME):latest"
                            $qualifiedTagBuild = "docker.io/library/$tagBuild"
                            $qualifiedTagLatest = "docker.io/library/$tagLatest"
                            docker build --tag $tagBuild --tag $tagLatest --tag $qualifiedTagBuild --tag $qualifiedTagLatest "$PWD"
                            minikube image load $qualifiedTagBuild
                            minikube image load $qualifiedTagLatest
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
                            kubectl config use-context minikube
                            kubectl cluster-info
                            kubectl apply -n ${K8S_NAMESPACE} -f k8s/deployment.yaml
                            kubectl apply -n ${K8S_NAMESPACE} -f k8s/service.yaml
                            kubectl set image deployment/${APP_NAME} ${APP_NAME}=${APP_NAME}:${IMAGE_TAG} -n ${K8S_NAMESPACE}
                            kubectl rollout status deployment/${APP_NAME} -n ${K8S_NAMESPACE} --timeout=180s
                        '''
                    } else {
                        powershell '''
                            $ErrorActionPreference = "Stop"
                            $imageRef = "docker.io/library/$($env:APP_NAME):$($env:IMAGE_TAG)"
                            minikube update-context -p minikube
                            kubectl config use-context minikube
                            kubectl cluster-info
                            kubectl apply -n $env:K8S_NAMESPACE -f k8s/deployment.yaml
                            kubectl apply -n $env:K8S_NAMESPACE -f k8s/service.yaml
                            $patch = "{""spec"":{""template"":{""spec"":{""containers"":[{""name"":""$($env:APP_NAME)"",""image"":""$imageRef""}]}}}}"
                            kubectl patch deployment $env:APP_NAME -n $env:K8S_NAMESPACE -p $patch
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
                    powershell(returnStatus: true, script: '$ErrorActionPreference = "Continue"; kubectl get pods -n $env:K8S_NAMESPACE -o wide 2>$null')
                }
            }
        }
    }
}
