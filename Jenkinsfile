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
                            set -e
                            TAG_BUILD="${APP_NAME}:${IMAGE_TAG}"
                            TAG_LATEST="${APP_NAME}:latest"
                            QUALIFIED_TAG_BUILD="docker.io/library/${TAG_BUILD}"
                            QUALIFIED_TAG_LATEST="docker.io/library/${TAG_LATEST}"
                            docker build -t "${TAG_BUILD}" -t "${TAG_LATEST}" -t "${QUALIFIED_TAG_BUILD}" -t "${QUALIFIED_TAG_LATEST}" .
                            minikube image load "${QUALIFIED_TAG_BUILD}"
                            minikube image load "${QUALIFIED_TAG_LATEST}"
                            minikube image ls | grep "${APP_NAME}" || true
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
                            minikube image ls | Select-String $env:APP_NAME
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
                            set -e
                            IMAGE_REF="docker.io/library/${APP_NAME}:${IMAGE_TAG}"
                            kubectl config use-context minikube
                            kubectl cluster-info
                            kubectl apply -n ${K8S_NAMESPACE} -f k8s/deployment.yaml
                            kubectl apply -n ${K8S_NAMESPACE} -f k8s/service.yaml
                            kubectl patch deployment "${APP_NAME}" -n "${K8S_NAMESPACE}" --type strategic -p "{\"spec\":{\"template\":{\"spec\":{\"containers\":[{\"name\":\"${APP_NAME}\",\"image\":\"${IMAGE_REF}\"}]}}}}"
                            kubectl get deployment "${APP_NAME}" -n "${K8S_NAMESPACE}" -o jsonpath='{.spec.template.spec.containers[0].image}'
                            echo
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
                            kubectl set image "deployment/$($env:APP_NAME)" "$($env:APP_NAME)=$imageRef" -n $env:K8S_NAMESPACE
                            $deployedImage = kubectl get deployment $env:APP_NAME -n $env:K8S_NAMESPACE -o jsonpath='{.spec.template.spec.containers[0].image}'
                            Write-Host "Deployment image: $deployedImage"
                            if ($deployedImage -ne $imageRef) {
                                throw "Deployment image mismatch. Expected $imageRef but found $deployedImage"
                            }
                            Write-Host ""
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
