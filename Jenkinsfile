pipeline {
    agent any

    options {
        // Prevent Jenkins from checking out SCM twice because we have an explicit stage for it.
        skipDefaultCheckout(true)
    }

    environment {
        // Shared names used across Docker image tags and Kubernetes resources.
        APP_NAME = 'cicdpractice'
        K8S_NAMESPACE = 'default'
        // BUILD_NUMBER is a Jenkins-provided value, so each run gets a unique image tag.
        IMAGE_TAG = "${BUILD_NUMBER}"
    }

    stages {
        stage('Preflight') {
            steps {
                script {
                    if (isUnix()) {
                        sh '''
                            set -e
                            # Fail early if any required CLI is missing on the Jenkins agent.
                            which git && git --version
                            which mvn && mvn -version
                            which docker && docker --version
                            which kubectl && kubectl version --client
                            which minikube && minikube version
                        '''
                    } else {
                        bat '''
                            REM Fail early if any required CLI is missing on the Jenkins agent.
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
                            # The pipeline targets the local Minikube cluster, so verify it is healthy first.
                            minikube status -p minikube
                            kubectl config use-context minikube
                            kubectl cluster-info
                        '''
                    } else {
                        powershell '''
                            $ErrorActionPreference = "Stop"
                            # Refresh kubeconfig for the Jenkins user before any deployment command runs.
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
                            # Build both short and fully-qualified tags so Kubernetes resolves the same image name
                            # that Minikube has in its runtime image store.
                            TAG_BUILD="${APP_NAME}:${IMAGE_TAG}"
                            TAG_LATEST="${APP_NAME}:latest"
                            QUALIFIED_TAG_BUILD="docker.io/library/${TAG_BUILD}"
                            QUALIFIED_TAG_LATEST="docker.io/library/${TAG_LATEST}"
                            docker build -t "${TAG_BUILD}" -t "${TAG_LATEST}" -t "${QUALIFIED_TAG_BUILD}" -t "${QUALIFIED_TAG_LATEST}" .
                            # `imagePullPolicy: Never` means the image must already exist inside Minikube.
                            minikube image load "${QUALIFIED_TAG_BUILD}"
                            minikube image load "${QUALIFIED_TAG_LATEST}"
                            minikube image ls | grep "${APP_NAME}" || true
                        '''
                    } else {
                        powershell '''
                            $ErrorActionPreference = "Stop"
                            # Build a unique image for this Jenkins run plus a convenience `latest` tag.
                            $tagBuild = "$($env:APP_NAME):$($env:IMAGE_TAG)"
                            $tagLatest = "$($env:APP_NAME):latest"
                            $qualifiedTagBuild = "docker.io/library/$tagBuild"
                            $qualifiedTagLatest = "docker.io/library/$tagLatest"
                            docker build --tag $tagBuild --tag $tagLatest --tag $qualifiedTagBuild --tag $qualifiedTagLatest "$PWD"
                            # Load the exact fully-qualified names that Kubernetes will reference.
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
                            # Apply the base manifests first, then override the image with the current build tag.
                            kubectl apply -n ${K8S_NAMESPACE} -f k8s/deployment.yaml
                            kubectl apply -n ${K8S_NAMESPACE} -f k8s/service.yaml
                            kubectl patch deployment "${APP_NAME}" -n "${K8S_NAMESPACE}" --type strategic -p "{\"spec\":{\"template\":{\"spec\":{\"containers\":[{\"name\":\"${APP_NAME}\",\"image\":\"${IMAGE_REF}\"}]}}}}"
                            # Print the deployment image so the build log shows exactly what Kubernetes accepted.
                            kubectl get deployment "${APP_NAME}" -n "${K8S_NAMESPACE}" -o jsonpath='{.spec.template.spec.containers[0].image}'
                            echo
                            # Rollout status is the real deployment success signal, not just `kubectl apply`.
                            kubectl rollout status deployment/${APP_NAME} -n ${K8S_NAMESPACE} --timeout=180s
                        '''
                    } else {
                        powershell '''
                            $ErrorActionPreference = "Stop"
                            $imageRef = "docker.io/library/$($env:APP_NAME):$($env:IMAGE_TAG)"
                            minikube update-context -p minikube
                            kubectl config use-context minikube
                            kubectl cluster-info
                            # Apply the stable manifests, then switch the Deployment to this build's image.
                            kubectl apply -n $env:K8S_NAMESPACE -f k8s/deployment.yaml
                            kubectl apply -n $env:K8S_NAMESPACE -f k8s/service.yaml
                            kubectl set image "deployment/$($env:APP_NAME)" "$($env:APP_NAME)=$imageRef" -n $env:K8S_NAMESPACE
                            # Fail fast if Kubernetes did not actually change the deployment template image.
                            $deployedImage = kubectl get deployment $env:APP_NAME -n $env:K8S_NAMESPACE -o jsonpath='{.spec.template.spec.containers[0].image}'
                            Write-Host "Deployment image: $deployedImage"
                            if ($deployedImage -ne $imageRef) {
                                throw "Deployment image mismatch. Expected $imageRef but found $deployedImage"
                            }
                            Write-Host ""
                            # Wait until the new pods are ready, otherwise Jenkins should mark the run as failed.
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
                        # For local Minikube use, print a quick access hint after a successful rollout.
                        IP="$(minikube ip)"
                        echo "Deployment successful. Access URL: http://${IP}:30007"
                    '''
                } else {
                    powershell '''
                        # On Windows, direct NodePort access may still vary by driver/networking, but the IP is useful.
                        $ip = minikube ip
                        Write-Host "Deployment successful. Access URL: http://$ip`:30007"
                    '''
                }
            }
        }
        always {
            script {
                if (isUnix()) {
                    // Keep pod status in the Jenkins log for quick rollout debugging.
                    sh(returnStatus: true, script: 'kubectl get pods -n ${K8S_NAMESPACE} -o wide')
                } else {
                    // Do not fail the whole build from this post-step; it is only for diagnostics.
                    powershell(returnStatus: true, script: '$ErrorActionPreference = "Continue"; kubectl get pods -n $env:K8S_NAMESPACE -o wide 2>$null')
                }
            }
        }
    }
}
