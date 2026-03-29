# CICDPractice

CI/CD setup for a Spring Boot app using Git, GitHub, Jenkins, Docker, and Kubernetes on Minikube.

## Repository

- GitHub: `https://github.com/deepaksahani877/cicd-practice.git`

## What this pipeline does

1. Jenkins fetches the latest code from GitHub.
2. Jenkins verifies required tools are installed.
3. Jenkins checks Minikube cluster health.
4. Jenkins runs `mvn clean verify`.
5. Jenkins builds Docker image tags for the current build.
6. Jenkins loads the image into Minikube.
7. Jenkins applies Kubernetes manifests.
8. Jenkins patches the deployment to the new image.
9. Jenkins waits for rollout completion.

## Recommended environment

Use Jenkins installed directly on Windows, not Jenkins inside Docker.

Why:
- local Docker Desktop, Minikube, `kubectl`, and Maven are easier to access
- avoids container PATH issues like `mvn not found` and `kubectl not found`
- avoids extra Docker socket and kubeconfig mounting

## Required tools

Install these on the Jenkins machine:

- Git
- Java 21
- Maven 3.9+
- Docker Desktop
- Minikube
- kubectl
- Jenkins
- ngrok

Verify them:

```powershell
git --version
java -version
mvn -version
docker --version
kubectl version --client
minikube version
ngrok version
```

## Clone the project

```powershell
git clone https://github.com/deepaksahani877/cicd-practice.git
cd cicd-practice
```

## Start Minikube and verify cluster

```powershell
minikube start -p minikube --driver=docker
minikube status -p minikube
minikube update-context -p minikube
kubectl config use-context minikube
kubectl get ns
kubectl get nodes
kubectl cluster-info
```

Expected:
- Minikube status should be `Running`
- context should be `minikube`
- namespace `default` should exist

## Install and run Jenkins on Windows

Install Jenkins LTS MSI from:

- `https://www.jenkins.io/download/`

Useful commands:

```powershell
Get-Service jenkins
Start-Service jenkins
Restart-Service jenkins
```

Check Jenkins password:

```powershell
Get-Content "C:\ProgramData\Jenkins\.jenkins\secrets\initialAdminPassword"
```

Check Jenkins port:

```powershell
Get-Content "C:\Program Files\Jenkins\jenkins.xml"
netstat -ano | findstr :8081
```

In this project, Jenkins was configured on `8081`.

## Jenkins setup

### Plugins

Install:

- Pipeline
- Git
- GitHub
- GitHub Integration
- Credentials
- Credentials Binding
- Pipeline: Stage View

### Global Tool Configuration

Set:

- Git -> installed `git.exe`
- JDK -> Java 21
- Maven -> installed Maven 3.9+

### Jenkins service account

If Jenkins cannot use Docker or Minikube, run the service as your normal Windows user:

1. Open `services.msc`
2. Open `jenkins` properties
3. Go to `Log On`
4. Select `This account`
5. Enter your Windows username and password
6. Restart Jenkins

## Create the pipeline job

Create a Pipeline job with these settings:

1. Name: `CICD practice`
2. General:
   - optional description
   - `Discard old builds`
   - `Do not allow concurrent builds`
3. Build Triggers:
   - `GitHub hook trigger for GITScm polling`
4. Pipeline:
   - Definition: `Pipeline script from SCM`
   - SCM: `Git`
   - Repository URL: `https://github.com/deepaksahani877/cicd-practice.git`
   - Branch: `*/main`
   - Script Path: `Jenkinsfile`

## Expose local Jenkins with ngrok

If GitHub needs to reach your local Jenkins:

```powershell
ngrok config add-authtoken <YOUR_NGROK_TOKEN>
ngrok http 8081
```

Use the HTTPS forwarding URL in GitHub webhook:

- `https://<your-ngrok-domain>/github-webhook/`

Keep ngrok running.

## GitHub webhook

In GitHub repo settings:

1. Open `Settings -> Webhooks`
2. Click `Add webhook`
3. Payload URL:
   - `https://<your-ngrok-domain>/github-webhook/`
4. Content type:
   - `application/json`
5. Event:
   - `Just the push event`
6. Active:
   - checked

## Current Jenkins pipeline commands

These are the commands used by the pipeline.

### Preflight

```powershell
where git && git --version
where mvn && mvn -version
where docker && docker --version
where kubectl && kubectl version --client
where minikube && minikube version
```

### Cluster Ready Check

```powershell
minikube status -p minikube
minikube update-context -p minikube
kubectl config use-context minikube
kubectl cluster-info
```

### Build and Test

```powershell
mvn clean verify
```

### Build Docker Image and Load into Minikube

```powershell
$tagBuild = "cicdpractice:$env:BUILD_NUMBER"
$tagLatest = "cicdpractice:latest"
$qualifiedTagBuild = "docker.io/library/$tagBuild"
$qualifiedTagLatest = "docker.io/library/$tagLatest"
docker build --tag $tagBuild --tag $tagLatest --tag $qualifiedTagBuild --tag $qualifiedTagLatest "$PWD"
minikube image load $qualifiedTagBuild
minikube image load $qualifiedTagLatest
docker images | Select-String cicdpractice
```

### Deploy to Kubernetes

```powershell
$imageRef = "docker.io/library/cicdpractice:$env:BUILD_NUMBER"
minikube update-context -p minikube
kubectl config use-context minikube
kubectl cluster-info
kubectl apply -n default -f k8s\deployment.yaml
kubectl apply -n default -f k8s\service.yaml
$patch = "{""spec"":{""template"":{""spec"":{""containers"":[{""name"":""cicdpractice"",""image"":""$imageRef""}]}}}}"
kubectl patch deployment cicdpractice -n default -p $patch
kubectl rollout status deployment/cicdpractice -n default --timeout=180s
```

### Post check

```powershell
kubectl get pods -n default -o wide
minikube ip
```

## Manual full run from terminal

If you want to run the same flow without Jenkins:

```powershell
mvn clean verify
$tag = Get-Date -Format 'yyyyMMddHHmmss'
docker build --tag "cicdpractice:$tag" --tag "cicdpractice:latest" --tag "docker.io/library/cicdpractice:$tag" --tag "docker.io/library/cicdpractice:latest" .
minikube update-context -p minikube
kubectl config use-context minikube
kubectl cluster-info
minikube image load "docker.io/library/cicdpractice:$tag"
minikube image load "docker.io/library/cicdpractice:latest"
kubectl apply -n default -f k8s\deployment.yaml
kubectl apply -n default -f k8s\service.yaml
$patch = "{""spec"":{""template"":{""spec"":{""containers"":[{""name"":""cicdpractice"",""image"":""docker.io/library/cicdpractice:$tag""}]}}}}"
kubectl patch deployment cicdpractice -n default -p $patch
kubectl rollout status deployment/cicdpractice -n default --timeout=180s
kubectl get deploy,pods,svc -n default
```

## Access the application

Check service:

```powershell
kubectl get svc -n default
minikube ip
```

Possible access patterns:

- `http://<minikube-ip>:30007`
- `minikube service cicdpractice-service -n default --url`

On Windows with Docker driver, `minikube service --url` may return a localhost tunnel URL like:

- `http://127.0.0.1:55998`

Keep that terminal open.

## Common issues

- `ErrImageNeverPull`
  - deployment is pointing to an image name not present inside Minikube
  - check:
  - `minikube image ls | Select-String cicdpractice`
  - `kubectl get pod <pod-name> -n default -o yaml`

- rollout hangs at `1 out of 2 new replicas`
  - one new pod is not becoming Ready
  - inspect:
  - `kubectl get deploy,rs,pods -n default -o wide`
  - `kubectl describe deployment cicdpractice -n default`
  - `kubectl get events -n default --sort-by=.metadata.creationTimestamp`

- Jenkins starts but UI not on expected port
  - inspect:
  - `Get-Content "C:\Program Files\Jenkins\jenkins.xml"`

- webhook works but build still uses old pipeline
  - latest local changes were not committed and pushed
  - check:
  - `git status --short`
  - `git log --oneline -n 5`

## Files

- `Jenkinsfile`
- `Dockerfile`
- `k8s/deployment.yaml`
- `k8s/service.yaml`
- `TUTORIAL.md`
- `INTERVIEW.md`
- `CHEATSHEET.md`
