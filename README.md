# CICDPractice

Complete CI/CD setup for Spring Boot using Git, GitHub, Jenkins, Docker, and Kubernetes (Minikube).

## GitHub Repository

- Repo URL: `https://github.com/deepaksahani877/cicd-practice.git`

## What this pipeline does

1. Pull source from GitHub
2. Build and test with Maven (`mvn clean verify`)
3. Build Docker image in Minikube Docker daemon
4. Deploy to Kubernetes using manifests in `k8s/`
5. Roll out the latest image tag (`cicdpractice:<build-number>`)

## 1. Clone project

```bash
git clone https://github.com/deepaksahani877/cicd-practice.git
cd cicd-practice
```

## 2. Install prerequisites

Install these on the same machine where Jenkins agent runs:

- Git
- JDK 21
- Maven 3.9+
- Docker
- Minikube
- kubectl
- Jenkins

Verify:

```bash
git --version
java -version
mvn -version
docker --version
minikube version
kubectl version --client
```

## 3. Start Docker and Minikube

```bash
minikube start
kubectl get nodes
```

Expected: node `minikube` should be `Ready`.

## 4. Start Jenkins

Open Jenkins at:

- `http://localhost:8080`

Install suggested plugins at first launch, then create admin user.

Required plugins:

- Pipeline
- Git
- GitHub Integration

## 5. Configure Jenkins pipeline job

1. Jenkins -> New Item -> `Pipeline` -> name it `CICDPractice`.
2. Under Pipeline:
   - Definition: `Pipeline script from SCM`
   - SCM: `Git`
   - Repository URL: `https://github.com/deepaksahani877/cicd-practice.git`
   - Branch: `*/main`
   - Script Path: `Jenkinsfile`
3. Save.

## 6. Configure GitHub webhook

In GitHub repo:

1. Settings -> Webhooks -> Add webhook
2. Payload URL: `http://<your-jenkins-host>:8080/github-webhook/`
3. Content type: `application/json`
4. Events: `Just the push event`
5. Save

If Jenkins is on your local machine and GitHub cannot access it, use `ngrok` or deploy Jenkins on a reachable server/VM.

## 7. Trigger pipeline

Option A: from Jenkins UI -> `Build Now`

Option B: push commit to GitHub:

```bash
git add .
git commit -m "trigger: test ci cd"
git push origin main
```

## 8. Verify CI/CD results

Check Jenkins stages:

- Checkout
- Build and Test
- Build Docker Image (Minikube)
- Deploy to Kubernetes

Check Kubernetes:

```bash
kubectl get deploy,pods,svc -n default
kubectl rollout status deployment/cicdpractice -n default
```

Access app:

```bash
minikube service cicdpractice-service --url
```

If command hangs on Windows networking, use:

```bash
minikube ip
```

Then open:

- `http://<minikube-ip>:30007`

## 9. Important project files

- `Jenkinsfile` - Pipeline definition
- `Dockerfile` - App image build
- `k8s/deployment.yaml` - Kubernetes deployment (`cicdpractice`)
- `k8s/service.yaml` - NodePort service (`cicdpractice-service`)

## 10. Common issues

- NodePort conflict (`30007 already allocated`):
  - Delete old service using same NodePort:
  - `kubectl delete svc springboot-service -n default`
- Jenkins cannot run Docker/Minikube:
  - Ensure Jenkins agent user has permissions for Docker and local tools
- Webhook not triggering:
  - Verify webhook delivery logs in GitHub
  - Verify Jenkins URL is publicly reachable

## 11. Manual fallback deploy commands

Use this when you want to test CD without Jenkins:

```bash
mvn clean verify
minikube -p minikube docker-env --shell powershell | Invoke-Expression
docker build -t cicdpractice:manual -t cicdpractice:latest .
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl set image deployment/cicdpractice cicdpractice=cicdpractice:manual -n default
kubectl rollout status deployment/cicdpractice -n default --timeout=180s
```
