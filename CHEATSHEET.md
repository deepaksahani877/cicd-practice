# CI/CD Interview Cheatsheet

This file is for fast revision. Each topic includes:

- `1-line answer`
- `2-minute answer`
- `5-minute direction`

## 1. What is CI/CD?

### 1-line answer

CI/CD is the practice of automatically building, testing, and deploying software so changes can move from commit to release quickly and reliably.

### 2-minute answer

CI means Continuous Integration, where developers merge code frequently and automated checks validate each change. CD means Continuous Delivery or Continuous Deployment, where validated changes are automatically prepared for release or deployed. The purpose is to reduce manual work, catch issues early, and improve release reliability.

### 5-minute direction

Explain:

- frequent integration
- automated build and test
- artifact generation
- deployment automation
- difference between delivery and deployment
- quality, speed, traceability, rollback

## 2. Why is CI/CD important?

### 1-line answer

It improves release speed, consistency, quality, and recovery by replacing manual and error-prone deployment steps with automation.

### 2-minute answer

Without CI/CD, teams rely on manual builds and deployments, which are slow and inconsistent. CI/CD gives early feedback, repeatable delivery, faster release cycles, and lower deployment risk. It also improves traceability because every release is tied to a specific commit, build, and artifact.

### 5-minute direction

Talk about:

- faster feedback
- lower integration risk
- auditability
- rollback confidence
- developer productivity
- standardization across teams

## 3. What is Jenkins?

### 1-line answer

Jenkins is an automation server used to orchestrate CI/CD pipelines.

### 2-minute answer

Jenkins automates build, test, packaging, and deployment workflows. It supports pipeline-as-code, plugins, credentials, distributed agents, and integration with GitHub, Docker, and Kubernetes. In our project it triggers on GitHub push, runs Maven, builds Docker images, and deploys to Minikube.

### 5-minute direction

Explain:

- controller and agents
- pipeline as code
- plugins
- credentials
- SCM triggers
- artifact and deployment orchestration

## 4. What is a Jenkinsfile?

### 1-line answer

A `Jenkinsfile` is a version-controlled pipeline definition written as code.

### 2-minute answer

Instead of configuring CI/CD logic manually in the Jenkins UI, we define it in a `Jenkinsfile` stored in Git. That makes the pipeline reviewable, reproducible, and easier to change. In this project the file defines preflight checks, cluster validation, build, image creation, and Kubernetes rollout.

### 5-minute direction

Cover:

- declarative vs scripted
- version control benefits
- review and rollback
- environment variables
- stages and post actions

## 5. What is Docker?

### 1-line answer

Docker packages an application and its runtime into a portable image that can run consistently across environments.

### 2-minute answer

Docker helps solve environment inconsistency by packaging the application, runtime, and dependencies together. We build an image once and run the same image in test, staging, or production. In this project, Docker packages the Spring Boot JAR into an image that Kubernetes can run.

### 5-minute direction

Explain:

- image vs container
- Dockerfile
- portability
- versioned tags
- container runtime consistency

## 6. What is Kubernetes?

### 1-line answer

Kubernetes is a platform that runs and manages containers at scale.

### 2-minute answer

Kubernetes handles deployment, scaling, restart, service discovery, and rolling updates for containers. Instead of manually running containers, you declare the desired state, and Kubernetes works to maintain it. In this project we use a Deployment for pods and a Service to expose the application.

### 5-minute direction

Discuss:

- pods
- deployments
- services
- ReplicaSets
- desired state
- health probes
- rolling updates

## 7. What is Minikube?

### 1-line answer

Minikube is a local single-node Kubernetes cluster used for development and learning.

### 2-minute answer

Minikube provides a lightweight local Kubernetes environment so you can test manifests and deployment pipelines without using cloud infrastructure. In this project it acts as the target cluster for Jenkins deployments.

### 5-minute direction

Mention:

- local learning cluster
- useful for demos and testing
- not equivalent to real production
- runtime and networking differences

## 8. What is a Docker image tag and why use unique tags?

### 1-line answer

An image tag identifies a specific build of a container image, and unique tags make deployments traceable and rollback-safe.

### 2-minute answer

Tags like `latest` are convenient but not reliable for production because they change over time. Unique tags such as build numbers or commit SHAs let you track exactly what was deployed and roll back to previous known-good versions.

### 5-minute direction

Cover:

- traceability
- rollback
- immutability
- why `latest` is risky
- registry consistency

## 9. What is `imagePullPolicy: Never`?

### 1-line answer

It tells Kubernetes not to pull the image from a registry and to run only if the image already exists on the node.

### 2-minute answer

This setting is useful in local Minikube workflows where images are built locally and loaded into the cluster manually. It is usually not appropriate for cloud production, where nodes should pull images from a registry like ECR.

### 5-minute direction

Explain:

- local image workflow
- dependency on node-local image presence
- why `ErrImageNeverPull` happens
- why cloud setups usually use `IfNotPresent` or `Always`

## 10. What are readiness and liveness probes?

### 1-line answer

Readiness decides when a pod can receive traffic, and liveness decides when a container should be restarted.

### 2-minute answer

Readiness probes protect users from traffic being routed to an app that is still starting or unhealthy. Liveness probes help recover from hung or dead containers. Together they improve reliability during rollout and runtime.

### 5-minute direction

Mention:

- startup behavior
- traffic gating
- self-healing
- misconfigured probes causing false failures

## 11. What is a webhook?

### 1-line answer

A webhook is an event-driven HTTP callback that triggers automation when something happens, such as a GitHub push.

### 2-minute answer

Instead of Jenkins constantly checking GitHub for changes, GitHub can notify Jenkins immediately via webhook. That reduces wasted polling and speeds up the build trigger.

### 5-minute direction

Explain:

- event source
- webhook endpoint
- payload delivery
- common failures like wrong URL or inaccessible local Jenkins

## 12. Why use `Pipeline script from SCM`?

### 1-line answer

Because the pipeline should be version-controlled and fetched from the same repository as the application.

### 2-minute answer

This keeps pipeline logic in Git along with the source code, which means changes are reviewed, auditable, and consistent across environments. It also prevents hidden logic from drifting in Jenkins UI configuration.

### 5-minute direction

Talk about:

- pipeline as code
- change review
- rollback
- portability

## 13. Why did rollout fail even though build succeeded?

### 1-line answer

Because successful build only proves the artifact was created, not that Kubernetes can access and run it correctly.

### 2-minute answer

In our case Maven build and Docker image build succeeded, but deployment or service behavior still failed because Kubernetes either received the wrong image reference or continued serving an older image. Build success does not guarantee deployability. Deployment still depends on image naming, runtime availability, manifest correctness, and rollout health.

### 5-minute direction

Explain:

- separation of CI and CD
- artifact correctness vs runtime correctness
- image availability
- manifest correctness
- readiness and rollout validation

## 14. Why can rollout hang at `1 out of 2 new replicas`?

### 1-line answer

Because one new pod is failing to become Ready, so Kubernetes keeps old pods running and waits for the new one.

### 2-minute answer

During a rolling update, Kubernetes gradually replaces old pods with new ones. If a new pod fails due to image issues, config problems, or probe failures, rollout pauses while old healthy pods continue serving traffic. This is a safety feature, not random hanging.

### 5-minute direction

Mention:

- deployment strategy
- ReplicaSet behavior
- readiness gating
- troubleshooting sequence

## 15. How do you debug `ErrImageNeverPull`?

### 1-line answer

I verify the exact image in the pod spec and check whether that same image exists in the cluster runtime.

### 2-minute answer

I inspect the failed pod YAML, deployment, and cluster events to confirm the image name. Then I compare it with what exists in Minikube or the registry. In our project, the root cause was a broken deploy image update and later a stale deployment still serving `latest`, so the right checks were deployment image, pod image, and live service response.

### 5-minute direction

Walk through:

- `kubectl get pod -o yaml`
- `kubectl describe deployment`
- `kubectl get events`
- `minikube image ls`
- fixing image naming and patching

## 16. Why use immutable image tags?

### 1-line answer

Immutable tags ensure each deployment points to one exact build and makes rollback and auditing reliable.

### 2-minute answer

If tags are reused, especially `latest`, it becomes hard to know what code is actually running. Immutable tags like build numbers or commit SHAs solve that by giving every image a unique identity that can be traced back to source and pipeline run.

### 5-minute direction

Mention:

- traceability
- rollback
- reproducibility
- debugging incidents

## 17. What is the difference between local Minikube and production EKS?

### 1-line answer

Minikube is a local single-node development cluster, while EKS is a managed production-grade Kubernetes platform on AWS.

### 2-minute answer

Minikube is useful for local testing, but EKS introduces multi-node scheduling, IAM, ECR integration, managed control plane, ingress, autoscaling, and production networking concerns. The workflow concept stays similar, but production architecture is much more complex.

### 5-minute direction

Cover:

- cluster scale
- registry integration
- IAM
- networking
- observability
- HA and DR

## 18. How would you move this pipeline to AWS?

### 1-line answer

Replace local Minikube with EKS and local images with ECR, while keeping the same build-test-deploy flow.

### 2-minute answer

I would build the image in Jenkins, tag it immutably, push it to ECR, and deploy it to EKS using manifests or Helm. I would update the image path, use AWS IAM-based auth, manage secrets securely, and expose the service through ALB or ingress rather than NodePort.

### 5-minute direction

Explain:

- ECR
- EKS
- IAM or IRSA
- Secrets Manager
- Ingress or ALB
- environment promotion

## 19. How do you answer scenario questions strongly?

### 1-line answer

State the symptom, explain the likely cause, describe how you would verify it, and then propose the fix.

### 2-minute answer

A strong scenario answer should not stop at definitions. You should say what you would inspect first, how you would reduce uncertainty, and how you would stabilize the system. For example, if rollout is stuck, I would inspect deployment, ReplicaSets, pods, events, and image availability before deciding whether to rollback or patch forward.

### 5-minute direction

Use this structure:

1. confirm impact
2. identify likely failure domain
3. inspect evidence
4. stabilize system
5. fix root cause
6. prevent recurrence

## 20. Sample quick spoken answers

### Q. Why Jenkins and not only shell scripts?

1-line:
- Jenkins gives orchestration, visibility, triggers, credentials, and repeatability around shell commands.

2-minute:
- Shell scripts can do the work, but Jenkins adds scheduling, webhooks, logs, stage visibility, agent control, credentials handling, and pipeline-as-code. It turns scripts into an operational delivery system.

### Q. Why Kubernetes and not Docker alone?

1-line:
- Docker runs containers, but Kubernetes manages them at deployment scale.

2-minute:
- Docker is the packaging and runtime layer, while Kubernetes handles replica management, service discovery, rolling updates, self-healing, and health checks. They solve different parts of the deployment problem.

### Q. What is the best way to prepare for a CI/CD interview?

1-line:
- Understand concepts, commands, and failure modes together.

2-minute:
- Interviewers usually test whether you can explain what a tool does, how you used it, and how you debugged it under failure. Memorizing definitions is not enough. Real examples from your pipeline issues make your answers much stronger.

## 21. Final revision checklist

Before interview, revise these:

1. CI vs CD
2. webhook vs polling
3. Jenkinsfile purpose and stages
4. Docker image vs container
5. Kubernetes Deployment, Service, Pod
6. readiness vs liveness probe
7. Minikube image loading
8. `ErrImageNeverPull`
9. rollout debugging steps
10. immutable image tags
11. ECR and EKS basics
12. rollback and production design thinking
