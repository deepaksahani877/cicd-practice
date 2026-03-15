# CICDPractice

CI/CD setup for Spring Boot using Git, GitHub, Jenkins, Docker, and Kubernetes (Minikube).

## GitHub Repository

- `https://github.com/deepaksahani877/cicd-practice.git`

## Architecture

1. Push code to GitHub `main`
2. GitHub webhook triggers Jenkins
3. Jenkins runs `Jenkinsfile`
4. Maven build/test
5. Docker image build
6. Kubernetes deploy rollout on Minikube

## Recommended Setup

Use Jenkins installed on Windows host (not Jenkins-in-Docker) for local Minikube workflow.

Reason:
- Jenkins-in-Docker often fails with `mvn: not found`, `kubectl: not found`, `minikube: not found` unless custom image/tooling is added.

## 1. Install Prerequisites

Install on the same machine where Jenkins runs:

- Git
- Java JDK 21
- Maven 3.9+
- Docker Desktop
- Minikube
- kubectl
- Jenkins (Windows MSI)
- ngrok

Verify:

```powershell
git --version
java -version
mvn -version
docker --version
minikube version
kubectl version --client
ngrok version
```

## 2. Clone Project

```powershell
git clone https://github.com/deepaksahani877/cicd-practice.git
cd cicd-practice
```

## 3. Start Minikube

```powershell
minikube start
kubectl get nodes
```

Expected: `minikube` node should be `Ready`.

## 4. Install and Start Jenkins on Windows

1. Install Jenkins LTS (MSI) from `https://www.jenkins.io/download/`.
2. Start service and confirm:

```powershell
Get-Service jenkins
Start-Service jenkins
```

3. Open `http://localhost:8080`.
4. Unlock Jenkins using:

```powershell
Get-Content "C:\ProgramData\Jenkins\.jenkins\secrets\initialAdminPassword"
```

5. Install suggested plugins and create admin user.

## 5. Jenkins Plugins

Install:

- Pipeline
- Git
- GitHub
- GitHub Integration
- Credentials
- Credentials Binding
- Pipeline: Stage View

## 6. Jenkins Global Tool Configuration

Manage Jenkins -> Global Tool Configuration:

- Git: path to `git.exe`
- JDK: JDK 21
- Maven: Maven 3.9+

## 7. Jenkins Service Account (Important)

If Jenkins cannot access Docker/Minikube, run Jenkins service as your Windows user:

1. Open `services.msc`
2. Jenkins -> Properties -> Log On
3. Select `This account` and provide your Windows username/password
4. Restart Jenkins service

## 8. Create Jenkins Pipeline Job

1. Jenkins Dashboard -> New Item
2. Name: `cicd-practice`
3. Type: `Pipeline`
4. General:
   - Check `Discard old builds` (recommended)
   - Check `Do not allow concurrent builds` (recommended)
5. Build Triggers:
   - Check `GitHub hook trigger for GITScm polling`
6. Pipeline:
   - Definition: `Pipeline script from SCM`
   - SCM: `Git`
   - Repo URL: `https://github.com/deepaksahani877/cicd-practice.git`
   - Branch: `*/main`
   - Script Path: `Jenkinsfile`
7. Save

## 9. Expose Local Jenkins with ngrok

```powershell
ngrok config add-authtoken <YOUR_NGROK_TOKEN>
ngrok http 8080
```

Copy HTTPS forwarding URL, for example:
- `https://xxxx-xx-xx-xx-xx.ngrok-free.app`

Keep ngrok terminal running.

## 10. Add GitHub Webhook

In GitHub repo:

1. Settings -> Webhooks -> Add webhook
2. Payload URL:
   - `https://<your-ngrok-url>/github-webhook/`
3. Content type:
   - `application/json`
4. Events:
   - `Just the push event`
5. Active:
   - checked
6. Save

Check delivery status in GitHub webhook panel. It should return HTTP `200`.

## 11. Trigger and Validate Pipeline

Push a commit:

```powershell
git add .
git commit -m "test: webhook ci cd run"
git push origin main
```

Expected Jenkins stages:

- Preflight
- Checkout
- Build and Test
- Build Docker Image (Minikube)
- Deploy to Kubernetes

Validate deploy:

```powershell
kubectl get deploy,pods,svc -n default
kubectl rollout status deployment/cicdpractice -n default
```

## 12. Access Application

Preferred on Windows:

```powershell
minikube service cicdpractice-service -n default --url
```

This may return URL like `http://127.0.0.1:<random-port>`.
Keep that terminal open while testing.

Direct NodePort (may fail on Windows networking):

- `http://<minikube-ip>:30007`

## 13. Common Failures and Fixes

- `mvn: not found` or `kubectl: not found` in Jenkins log:
  - Jenkins runtime missing tools.
  - Install tools on Jenkins node, or run Jenkins on host instead of Docker container.

- `nodePort 30007 already allocated`:
  - Delete conflicting old service:
  - `kubectl delete svc springboot-service -n default`

- Webhook triggers but no build:
  - Verify job has `GitHub hook trigger for GITScm polling`.
  - Check GitHub webhook delivery logs for status code and response.

- `minikube service --url` prints localhost URL:
  - Normal with Docker driver on Windows.
  - Keep tunnel terminal open.

## 14. Important Files

- `Jenkinsfile`
- `Dockerfile`
- `k8s/deployment.yaml`
- `k8s/service.yaml`
