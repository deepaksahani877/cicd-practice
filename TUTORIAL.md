# Tutorial

This file explains the commands used in this project for CI/CD, debugging, and deployment.

## 1. Git commands

### Clone repository

```powershell
git clone https://github.com/deepaksahani877/cicd-practice.git
cd cicd-practice
```

Purpose:
- downloads the project from GitHub
- moves into the project folder

Use when:
- setting up the project on a new machine

### Check current changes

```powershell
git status --short
```

Purpose:
- shows modified, added, and untracked files

Use when:
- checking whether your latest `Jenkinsfile` or docs are committed

### View recent commits

```powershell
git log --oneline -n 5
```

Purpose:
- shows the latest commits in short format

Use when:
- confirming whether Jenkins should be picking up the latest pipeline changes

### Commit and push changes

```powershell
git add Jenkinsfile README.md TUTORIAL.md
git commit -m "docs: update ci cd setup and tutorial"
git push origin main
```

Purpose:
- stages file changes
- creates a commit
- uploads the commit to GitHub

Use when:
- you want Jenkins webhook builds to use the new pipeline or docs

## 2. Tool verification commands

### Check installed tools

```powershell
git --version
java -version
mvn -version
docker --version
kubectl version --client
minikube version
ngrok version
```

Purpose:
- verifies required tools are installed and reachable from PATH

Use when:
- setting up machine
- debugging Jenkins preflight failures

### Check command path on Windows

```powershell
where git
where mvn
where docker
where kubectl
where minikube
```

Purpose:
- shows the actual executable path being used

Use when:
- Jenkins finds a different tool than you expect

## 3. Jenkins commands and checks

### Check Jenkins service

```powershell
Get-Service jenkins
Start-Service jenkins
Restart-Service jenkins
```

Purpose:
- verifies or controls the Jenkins Windows service

Use when:
- Jenkins UI is not opening
- Jenkins needs a restart after service account changes

### Read Jenkins initial password

```powershell
Get-Content "C:\ProgramData\Jenkins\.jenkins\secrets\initialAdminPassword"
```

Purpose:
- reads the first-time Jenkins unlock password

Use when:
- opening Jenkins for the first time

### Check Jenkins port configuration

```powershell
Get-Content "C:\Program Files\Jenkins\jenkins.xml"
netstat -ano | findstr :8081
```

Purpose:
- reads the Windows service arguments used by Jenkins
- checks whether the configured port is listening

Use when:
- `localhost:8080` does not open
- Jenkins is actually on another port like `8081`

### Inspect job config on disk

```powershell
Get-Content "C:\ProgramData\Jenkins\.jenkins\jobs\CICD practice\config.xml"
```

Purpose:
- shows the pipeline job configuration Jenkins is using

Use when:
- verifying SCM URL, branch, webhook trigger, or script path

## 4. ngrok commands

### Start ngrok tunnel

```powershell
ngrok config add-authtoken <YOUR_NGROK_TOKEN>
ngrok http 8081
```

Purpose:
- creates a public HTTPS URL that forwards to local Jenkins

Use when:
- GitHub webhook needs to reach your local Jenkins

## 5. Minikube and Kubernetes commands

### Start Minikube

```powershell
minikube start -p minikube --driver=docker
```

Purpose:
- starts the local Kubernetes cluster

Use when:
- cluster is not running

### Check Minikube health

```powershell
minikube status -p minikube
```

Purpose:
- shows host, kubelet, apiserver, and kubeconfig state

Use when:
- rollout or `kubectl` is failing

### Update kube context

```powershell
minikube update-context -p minikube
kubectl config use-context minikube
```

Purpose:
- points `kubectl` to the Minikube cluster

Use when:
- `kubectl` points to the wrong server
- Jenkins service account has stale kubeconfig

### Check cluster info

```powershell
kubectl cluster-info
kubectl get ns
kubectl get nodes
```

Purpose:
- confirms cluster API, namespaces, and node health

Use when:
- starting cluster
- before deployment

### Access cluster resources

```powershell
kubectl get deploy,pods,svc -n default
kubectl get deploy,rs,pods -n default -o wide
kubectl describe deployment cicdpractice -n default
kubectl get events -n default --sort-by=.metadata.creationTimestamp
```

Purpose:
- checks the deployment, ReplicaSets, pods, services, and events

Use when:
- rollout hangs
- pods are not getting ready

### Inspect a failed pod

```powershell
kubectl get pod <pod-name> -n default -o yaml
```

Purpose:
- shows exact image, state, readiness, and error messages

Use when:
- debugging `ErrImageNeverPull`
- debugging readiness or crash issues

## 6. Maven commands

### Build and test

```powershell
mvn clean verify
```

Purpose:
- cleans old build output
- compiles the app
- runs tests
- produces the JAR used in Docker build

Use when:
- Jenkins `Build and Test` stage
- manual local verification

## 7. Docker commands

### Build application image

```powershell
docker build --tag cicdpractice:latest .
```

Purpose:
- builds container image from `Dockerfile`

Use when:
- local manual testing

### Build CI/CD image tags

```powershell
$tag = "20260329225419"
docker build --tag "cicdpractice:$tag" --tag "cicdpractice:latest" --tag "docker.io/library/cicdpractice:$tag" --tag "docker.io/library/cicdpractice:latest" .
```

Purpose:
- creates both short and fully qualified image tags
- makes the image usable by Minikube and Kubernetes

Use when:
- Jenkins build stage
- manual deployment with specific version tag

### List built images

```powershell
docker images | Select-String cicdpractice
```

Purpose:
- confirms image tags exist locally

Use when:
- debugging missing-image deploys

## 8. Minikube image commands

### Load image into Minikube

```powershell
minikube image load "docker.io/library/cicdpractice:latest"
minikube image load "docker.io/library/cicdpractice:20260329225419"
```

Purpose:
- copies the built Docker image into the Minikube runtime

Use when:
- `imagePullPolicy: Never`
- local images must exist inside Minikube

### List images inside Minikube

```powershell
minikube image ls | Select-String cicdpractice
```

Purpose:
- checks which images Minikube can actually run

Use when:
- pod says `ErrImageNeverPull`

## 9. Deployment commands

### Apply manifests

```powershell
kubectl apply -n default -f k8s\deployment.yaml
kubectl apply -n default -f k8s\service.yaml
```

Purpose:
- creates or updates deployment and service definitions

Use when:
- first deployment
- manifest updates

### Patch deployment image

```powershell
$imageRef = "docker.io/library/cicdpractice:20260329225419"
kubectl set image deployment/cicdpractice cicdpractice=$imageRef -n default
```

Purpose:
- updates the deployment to a specific image tag

Use when:
- every CI/CD run
- manual rollout to a new image

### Verify deployment image after update

```powershell
kubectl get deployment cicdpractice -n default -o jsonpath='{.spec.template.spec.containers[0].image}'
```

Purpose:
- confirms Kubernetes accepted the exact image reference you intended to deploy

Use when:
- rollout appears stuck
- build succeeded but application still serves old output

### Watch rollout

```powershell
kubectl rollout status deployment/cicdpractice -n default --timeout=180s
```

Purpose:
- waits until the new pods are ready

Use when:
- checking whether deployment really succeeded

If it waits too long:
- one or more pods are not becoming Ready
- inspect pods and events

## 10. Access commands

### Get Minikube IP

```powershell
minikube ip
```

Purpose:
- returns Minikube node IP

Use when:
- accessing NodePort service directly

### Get service URL

```powershell
minikube service cicdpractice-service -n default --url
```

Purpose:
- prints a reachable URL for the service

Use when:
- Windows Docker driver networking makes direct NodePort access unreliable

Note:
- on Windows, this may return `http://127.0.0.1:<random-port>`
- keep that terminal open

## 11. Common debugging patterns

### Pipeline uses old Jenkinsfile

Check:

```powershell
git status --short
git log --oneline -n 5
```

Reason:
- Jenkins fetches from GitHub, not your uncommitted local files

### Rollout stuck at `1 out of 2 new replicas`

Check:

```powershell
kubectl get deploy,rs,pods -n default -o wide
kubectl describe deployment cicdpractice -n default
kubectl get events -n default --sort-by=.metadata.creationTimestamp
```

Reason:
- one new replica is failing readiness, image load, or startup

### Build is successful but old response is still visible

Check:

```powershell
kubectl get deployment cicdpractice -n default -o jsonpath='{.spec.template.spec.containers[0].image}'
kubectl get pods -n default -l app=cicdpractice -o custom-columns=NAME:.metadata.name,READY:.status.containerStatuses[0].ready,IMAGE:.spec.containers[0].image
kubectl run tmp-curl --rm -i --tty --restart=Never --image=curlimages/curl:8.10.1 -- curl -sS http://cicdpractice-service.default.svc.cluster.local:8080/
```

Reason:
- Jenkins may have built the new image but Kubernetes is still serving an older ready ReplicaSet or an unchanged `latest` image

### Pod shows `ErrImageNeverPull`

Check:

```powershell
kubectl get pod <pod-name> -n default -o yaml
minikube image ls | Select-String cicdpractice
```

Reason:
- Kubernetes is using an image name not present inside Minikube

## 12. Current pipeline stages summary

1. `Preflight`
   - verifies tools exist
2. `Cluster Ready Check`
   - verifies Minikube and `kubectl` context
3. `Checkout`
   - pulls source from GitHub
4. `Build and Test`
   - runs Maven
5. `Build Docker Image (Minikube)`
   - builds and loads image into Minikube
6. `Deploy to Kubernetes`
   - applies manifests and rolls out new image
7. `Post`
   - prints pod status and access hint

## 13. File reference and configuration details

This section explains the Docker, Jenkins, and Kubernetes files in the repository line by line at a practical level.

### `Dockerfile`

Current file purpose:
- builds the runtime image for the Spring Boot application
- copies the packaged JAR from `target/`
- starts the application with `java -jar`

Important lines:

```dockerfile
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY ./target/CICDPractice-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
```

Explanation:

- `FROM eclipse-temurin:21-jdk-jammy`
  - base image used by Docker
  - `eclipse-temurin` is the Java distribution
  - `21` matches your project Java version
  - `jammy` is the Ubuntu 22.04 base layer

- `WORKDIR /app`
  - sets the working directory inside the container
  - all later commands run relative to `/app`

- `COPY ./target/CICDPractice-0.0.1-SNAPSHOT.jar app.jar`
  - copies the built JAR from host workspace into container
  - left side is host build output
  - right side is container path/name

- `ENTRYPOINT ["java","-jar","app.jar"]`
  - tells the container what to run when it starts
  - uses JSON array syntax so argument parsing is reliable

What is user-defined:

- base image choice
- Java version in the image
- destination working directory
- source JAR path
- entrypoint command

What is effectively reserved or system-constrained:

- `FROM` must refer to a valid image that Docker can pull or already has
- the copied JAR must actually exist after `mvn clean verify`
- the container command must be executable in that base image

What can be hardcoded safely:

- `/app`
- `app.jar`
- `java -jar`

What should be reviewed before hardcoding:

- exact JAR file name like `CICDPractice-0.0.1-SNAPSHOT.jar`
  - this breaks if artifactId or version changes
- base image tag
  - should match project Java version

Typical use case:
- Jenkins builds the JAR
- Dockerfile packages that JAR into a deployable image

### `Jenkinsfile`

Current file purpose:
- defines the CI/CD pipeline Jenkins executes
- uses declarative pipeline syntax
- supports both Unix and Windows steps
- is optimized for Windows Jenkins + Minikube

Top-level structure:

```groovy
pipeline {
    agent any
    options { ... }
    environment { ... }
    stages { ... }
    post { ... }
}
```

#### `agent any`

Meaning:
- Jenkins may run the pipeline on any available agent

User-defined:
- yes

When to change:
- if you want to force a labeled node like `windows`, `local-tools`, or `docker`

#### `options { skipDefaultCheckout(true) }`

Meaning:
- disables Jenkins automatic checkout before stages
- avoids duplicate SCM checkout because there is already an explicit `Checkout` stage

User-defined:
- yes

Safe to keep hardcoded:
- yes, for this pipeline

#### `environment`

Current values:

```groovy
environment {
    APP_NAME = 'cicdpractice'
    K8S_NAMESPACE = 'default'
    IMAGE_TAG = "${BUILD_NUMBER}"
}
```

Explanation:

- `APP_NAME`
  - logical application name
  - reused for deployment name, container name, and image naming

- `K8S_NAMESPACE`
  - target Kubernetes namespace
  - currently `default`

- `IMAGE_TAG`
  - Jenkins-generated build number
  - gives each build a unique image tag

What is user-defined:

- `APP_NAME`
- `K8S_NAMESPACE`

What is system-provided:

- `BUILD_NUMBER`
  - Jenkins automatically sets this at runtime

What can be hardcoded:

- app name and namespace for a small local project

What should usually remain dynamic:

- image tag
- build number

#### Stage: `Preflight`

Purpose:
- verify required tools are present on the Jenkins machine

Commands checked:
- `git`
- `mvn`
- `docker`
- `kubectl`
- `minikube`

Why this stage exists:
- fail early with a clear reason if Jenkins environment is incomplete

What is user-defined:
- which tools are checked

What is reserved/system behavior:
- `where` and `which` behavior depends on the OS

#### Stage: `Cluster Ready Check`

Purpose:
- verify Minikube is running and `kubectl` points to the correct cluster

Important commands:
- `minikube status -p minikube`
- `minikube update-context -p minikube`
- `kubectl config use-context minikube`
- `kubectl cluster-info`

What is user-defined:
- Minikube profile name
- target context name

What is system-defined:
- cluster endpoint URL returned by Minikube

What can be hardcoded:

- profile name `minikube`
  - safe in local setup if you always use the default profile

What may need changing:

- if you create a custom Minikube profile name

#### Stage: `Checkout`

Purpose:
- fetches the project source defined in Jenkins job SCM configuration

Command:

```groovy
checkout scm
```

Meaning:
- Jenkins uses the repo and branch configured in the job

What is user-defined:
- GitHub repository URL
- branch pattern

What is reserved/system behavior:
- `scm` object is injected by Jenkins for Pipeline from SCM jobs

#### Stage: `Build and Test`

Purpose:
- compile, test, and package the Spring Boot application

Command:

```groovy
mvn clean verify
```

Why `verify`:
- runs through compile, test, package, and verification lifecycle
- ensures the JAR exists before Docker build

What is user-defined:
- Maven goal selection

What is system-defined:
- Maven lifecycle behavior
- JAR output path under `target/`

#### Stage: `Build Docker Image (Minikube)`

Purpose:
- build image tags
- load them into Minikube runtime

Important Windows logic:

```powershell
$tagBuild = "$($env:APP_NAME):$($env:IMAGE_TAG)"
$tagLatest = "$($env:APP_NAME):latest"
$qualifiedTagBuild = "docker.io/library/$tagBuild"
$qualifiedTagLatest = "docker.io/library/$tagLatest"
docker build --tag $tagBuild --tag $tagLatest --tag $qualifiedTagBuild --tag $qualifiedTagLatest "$PWD"
minikube image load $qualifiedTagBuild
minikube image load $qualifiedTagLatest
```

Explanation:

- short tags like `cicdpractice:7`
  - useful locally

- fully qualified tags like `docker.io/library/cicdpractice:7`
  - avoid ambiguity when Kubernetes resolves image names

- `minikube image load`
  - copies local images into Minikube's container runtime

- `minikube image ls`
  - is better than only checking local Docker images because Kubernetes runs against Minikube's runtime, not just host Docker

What is user-defined:

- image name
- whether to keep `latest`
- whether to use fully qualified names

What is reserved/system behavior:

- Docker build context
- Minikube runtime storage

What should be hardcoded:

- image repository prefix `docker.io/library/` is acceptable in this local setup

What should stay dynamic:

- build tag based on Jenkins build number

#### Stage: `Deploy to Kubernetes`

Purpose:
- apply manifests and update deployment image to the current build

Important commands:

```powershell
kubectl apply -n $env:K8S_NAMESPACE -f k8s/deployment.yaml
kubectl apply -n $env:K8S_NAMESPACE -f k8s/service.yaml
kubectl set image deployment/$env:APP_NAME $env:APP_NAME=$imageRef -n $env:K8S_NAMESPACE
kubectl get deployment $env:APP_NAME -n $env:K8S_NAMESPACE -o jsonpath='{.spec.template.spec.containers[0].image}'
kubectl rollout status deployment/$env:APP_NAME -n $env:K8S_NAMESPACE --timeout=180s
```

Why `set image` plus verification is used:
- it updates only the image field without rewriting the full manifest
- verification immediately after update confirms Kubernetes actually accepted the expected image
- this is important because a successful build does not guarantee the cluster is running the new code

What is user-defined:

- namespace
- manifest paths
- deployment name
- rollout timeout

What is reserved/system behavior:

- Kubernetes rollout logic
- ReplicaSet generation and pod scheduling

What can be hardcoded:

- manifest paths
- namespace `default` for local learning setup

What should stay dynamic:

- image reference for each build

#### `post`

Purpose:
- show useful output after success or failure

Current behavior:
- on success, print Minikube IP
- always, print pod list

Use case:
- quick troubleshooting from Jenkins console output

### `k8s/deployment.yaml`

Current file:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: cicdpractice
  labels:
    app: cicdpractice
spec:
  replicas: 2
  selector:
    matchLabels:
      app: cicdpractice
  template:
    metadata:
      labels:
        app: cicdpractice
    spec:
      containers:
        - name: cicdpractice
          image: cicdpractice:latest
          imagePullPolicy: Never
          ports:
            - containerPort: 8080
          readinessProbe:
            tcpSocket:
              port: 8080
            initialDelaySeconds: 10
            periodSeconds: 5
          livenessProbe:
            tcpSocket:
              port: 8080
            initialDelaySeconds: 20
            periodSeconds: 10
```

Property explanation:

- `apiVersion: apps/v1`
  - required Kubernetes API group for deployments
  - reserved by Kubernetes API

- `kind: Deployment`
  - tells Kubernetes this resource is a Deployment
  - reserved Kubernetes object type

- `metadata.name`
  - deployment name
  - user-defined

- `metadata.labels`
  - identifying labels
  - user-defined

- `spec.replicas: 2`
  - number of desired pod replicas
  - user-defined

- `spec.selector.matchLabels`
  - labels the deployment uses to manage pods
  - must match pod template labels
  - user-defined, but structurally required by Kubernetes

- `spec.template.metadata.labels`
  - labels attached to pods
  - must stay consistent with selector

- `spec.template.spec.containers`
  - list of containers in each pod

- `containers[].name`
  - logical container name inside pod
  - user-defined

- `containers[].image`
  - default image from manifest
  - user-defined
  - Jenkins later overwrites this using `kubectl patch`

- `imagePullPolicy: Never`
  - Kubernetes must not pull from a registry
  - correct for local Minikube image loading
  - should not be used in real cloud production unless image is preloaded on nodes

- `containerPort: 8080`
  - app listening port inside container
  - user-defined, must match app behavior

- `readinessProbe`
  - tells Kubernetes when pod is ready to receive traffic
  - user-defined

- `livenessProbe`
  - tells Kubernetes when container should be restarted
  - user-defined

What is safe to hardcode:

- deployment name
- label names
- port `8080`
- probes for a stable app

What should be carefully chosen:

- `imagePullPolicy`
- replica count
- probe delays

What is system-reserved:

- `apiVersion`
- `kind`
- the general structure of deployment schema

### `k8s/service.yaml`

Current file:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: cicdpractice-service
spec:
  type: NodePort
  selector:
    app: cicdpractice
  ports:
    - port: 8080
      targetPort: 8080
      nodePort: 30007
```

Property explanation:

- `apiVersion: v1`
  - Kubernetes core API version for Service

- `kind: Service`
  - Kubernetes Service resource type

- `metadata.name`
  - service name
  - user-defined

- `spec.type: NodePort`
  - exposes service on a node port
  - useful for local Minikube access

- `selector.app: cicdpractice`
  - routes traffic to pods with label `app=cicdpractice`

- `port: 8080`
  - service port inside cluster

- `targetPort: 8080`
  - forwards traffic to container port 8080

- `nodePort: 30007`
  - external port on Minikube node
  - user-defined, but must be available and inside valid NodePort range

What is safe to hardcode:

- service name
- target port
- NodePort for stable local testing

What can conflict:

- `nodePort: 30007`
  - fails if another service already uses it

What is system-reserved:

- service schema keys
- valid NodePort range enforcement

### Which values are user-defined vs system-defined

User-defined in this project:

- app name `cicdpractice`
- deployment name
- service name
- namespace `default`
- port `8080`
- NodePort `30007`
- replica count `2`
- Docker image tags
- GitHub repository URL
- Jenkins job name

System-defined at runtime:

- Jenkins `BUILD_NUMBER`
- Minikube cluster endpoint URL
- pod names
- ReplicaSet names
- container IDs
- cluster IPs
- pod IPs

Reserved by the platforms:

- Dockerfile directives like `FROM`, `COPY`, `ENTRYPOINT`
- Jenkins declarative keywords like `pipeline`, `stages`, `post`
- Kubernetes object kinds and schemas like `Deployment`, `Service`, `apiVersion`, `spec`

### What should not be hardcoded carelessly

- image tag for CI/CD deployments
  - should change per build

- GitHub webhook tunnel URL
  - ngrok URLs change unless reserved

- Jenkins port
  - should match actual `jenkins.xml`

- NodePort
  - okay for local setup, but can conflict

- kube context
  - safe to hardcode `minikube` only if that is your known local cluster

### Good hardcoded values in this project

- `K8S_NAMESPACE=default`
- deployment name `cicdpractice`
- service name `cicdpractice-service`
- container port `8080`
- NodePort `30007`
- `imagePullPolicy: Never` for local Minikube workflow

### Values usually dynamic in production-grade pipelines

- image registry
- image tag
- namespace per environment
- replica count per environment
- ingress hostnames
- secrets and credentials
- webhook public URL
