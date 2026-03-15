# CICDPractice

CI/CD pipeline for a Spring Boot app using Git, GitHub, Jenkins, Docker, and Kubernetes (Minikube).

## What is configured

- `Jenkinsfile`
  - Checkout from GitHub (via SCM job config)
  - Build and test with Maven (`mvn clean verify`)
  - Build Docker image inside Minikube Docker daemon
  - Deploy to Kubernetes with rolling update
- `k8s/deployment.yaml`
  - Deployment name: `cicdpractice`
  - 2 replicas
  - Readiness/Liveness probes on port `8080`
- `k8s/service.yaml`
  - NodePort service: `cicdpractice-service`
  - Exposes app on NodePort `30007`

## Prerequisites

- Git installed
- GitHub repository containing this project
- Jenkins installed and running
- Docker installed
- Minikube installed and running
- `kubectl` configured for Minikube cluster
- Jenkins agent with these tools in `PATH`: `docker`, `kubectl`, `minikube`, `java`, `mvn`

## Jenkins setup

1. Create a **Pipeline** job in Jenkins.
2. In job config:
   - Pipeline definition: `Pipeline script from SCM`
   - SCM: `Git`
   - Repo URL: your GitHub repo URL
   - Script Path: `Jenkinsfile`
3. Add webhook in GitHub:
   - URL: `http://<jenkins-host>:8080/github-webhook/`
   - Content type: `application/json`
   - Event: `Just the push event`

## First run (local Minikube)

Run once on the Jenkins host:

```bash
minikube start
kubectl get nodes
```

Then trigger Jenkins build. Pipeline will:

1. Build and test app
2. Build Docker image `cicdpractice:<build-number>`
3. Apply manifests in `k8s/`
4. Update deployment image and wait for rollout

## Verify deployment

```bash
kubectl get deploy,pods,svc
minikube service cicdpractice-service --url
```

Open the URL returned by the second command.

## Notes

- `imagePullPolicy: Never` is intentional for local Minikube image usage.
- If Jenkins runs in a separate environment from Minikube, use a remote container registry instead of local Minikube Docker daemon.
