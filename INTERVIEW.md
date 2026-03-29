# CI/CD Interview Questions and Answers

This file is designed for a developer with around 5 years of experience preparing for CI/CD interviews. It covers Git, GitHub, Jenkins, Docker, Kubernetes, Minikube, and delivery scenarios from basic to advanced.

## 1. CI/CD fundamentals

### Q1. What is CI?

Answer:
CI stands for Continuous Integration. It is the practice of frequently merging code into a shared branch and automatically validating the changes through build, test, and quality checks. The goal is to detect integration issues early rather than after many changes accumulate.

### Q2. What is CD?

Answer:
CD can mean Continuous Delivery or Continuous Deployment.

- Continuous Delivery means every change is automatically built, tested, and prepared for release, but production deployment may still require approval.
- Continuous Deployment means every validated change is automatically deployed without manual approval.

### Q3. Why is CI/CD important?

Answer:
CI/CD reduces manual effort, shortens release cycles, standardizes quality checks, improves repeatability, and lowers the risk of broken deployments. It also gives teams faster feedback when a change introduces a problem.

### Q4. What are typical stages in a CI/CD pipeline?

Answer:
Typical stages are:

1. Checkout source code
2. Dependency restore
3. Build
4. Unit tests
5. Static analysis or security scan
6. Package artifact
7. Build container image
8. Push artifact or image
9. Deploy to target environment
10. Run post-deploy verification

### Q5. What is the difference between build, release, and deploy?

Answer:
- Build converts source code into an executable artifact like a JAR, binary, or image.
- Release marks a tested build as a candidate for delivery.
- Deploy places that artifact into an environment where it can run.

## 2. Git and GitHub

### Q6. How does Git support CI/CD?

Answer:
Git is the source of truth for code, configuration, and pipeline definitions. Every push can trigger automation. Branching, pull requests, commit history, and tags help control how code moves through environments.

### Q7. What is a webhook and why is it used in CI/CD?

Answer:
A webhook is an HTTP callback triggered by an event such as a GitHub push. Instead of Jenkins polling GitHub continuously, GitHub sends an event to Jenkins immediately, which is more efficient and faster.

### Q8. What is the difference between polling and webhook triggering?

Answer:
- Polling means Jenkins checks the repository at intervals to see if anything changed.
- Webhook triggering means GitHub informs Jenkins as soon as a change happens.

Webhook-based triggering is more efficient and lower latency.

### Q9. Why should pipeline code be stored in Git?

Answer:
Storing pipeline code in Git gives version control, peer review, rollback capability, and consistency across environments. It also keeps application logic and deployment logic aligned in the same repository.

### Q10. What is the advantage of using `main` plus feature branches in CI/CD?

Answer:
Feature branches isolate development work, while `main` stays stable and releasable. CI can validate both feature branches and `main`, reducing the chance of broken code reaching shared environments.

## 3. Jenkins basics

### Q11. What is Jenkins?

Answer:
Jenkins is an automation server used to orchestrate build, test, and deployment workflows. It supports pipelines, plugins, credentials management, triggers, and integration with many development tools.

### Q12. What is a Jenkins pipeline?

Answer:
A Jenkins pipeline is a script or workflow definition that describes the CI/CD process as code. It defines stages, steps, conditions, and post actions in a repeatable way.

### Q13. What is the difference between declarative and scripted pipeline?

Answer:
- Declarative pipeline uses a structured syntax with predefined sections such as `agent`, `stages`, and `post`. It is easier to read and standardize.
- Scripted pipeline is more flexible and Groovy-driven, but can become harder to maintain.

For most teams, declarative is preferred unless advanced control flow is required.

### Q14. What is the purpose of `agent any` in Jenkins?

Answer:
It tells Jenkins that the pipeline can run on any available agent. In larger systems, you may replace this with a labeled agent to ensure the correct OS and tools are used.

### Q15. Why use `Pipeline script from SCM` instead of writing pipeline code in the Jenkins UI?

Answer:
Because the pipeline becomes version-controlled, reviewable, reproducible, and easier to maintain. UI-entered pipeline logic tends to drift and is harder to audit.

### Q16. What is the purpose of the `environment` block in Jenkins?

Answer:
It defines environment variables shared across the pipeline, such as app names, namespace names, or dynamic tags. This avoids duplicated hardcoded values across stages.

### Q17. Why do we use a `Preflight` stage?

Answer:
A preflight stage fails early if essential tools like `mvn`, `docker`, `kubectl`, or `minikube` are missing. This shortens debugging time and makes errors clearer.

### Q18. Why was `skipDefaultCheckout(true)` used in this project?

Answer:
Jenkins automatically checks out SCM by default in declarative pipelines. This project uses an explicit `Checkout` stage, so `skipDefaultCheckout(true)` avoids duplicate cloning/fetching.

### Q19. What is the purpose of the `post` block in Jenkins?

Answer:
It defines actions to run after the main stages, such as printing pod status, cleanup, notifications, or logging deployment URLs.

### Q20. Why is a pipeline sometimes green for build but red overall?

Answer:
Because later stages like deploy or post-validation failed even though compilation and tests succeeded. Pipeline success depends on all required stages completing successfully.

## 4. Jenkins advanced and design questions

### Q21. Why should Jenkins run on a node with the correct tools rather than installing everything in every build?

Answer:
Tooling should be stable, predictable, and controlled at the agent level. Installing tools during every build increases runtime, creates drift, introduces network dependency, and makes failures less deterministic.

### Q22. What problems can happen if Jenkins runs inside Docker for local Minikube workflows?

Answer:
Common problems are:

- missing `mvn`, `kubectl`, or `minikube`
- no access to Docker daemon
- missing kubeconfig
- Minikube profile not visible inside container
- image loading problems between host and container runtimes

That is why Jenkins on Windows host was the better choice for this local setup.

### Q23. What is a Jenkins credential and why should it be used?

Answer:
A Jenkins credential is a securely stored secret such as a token, SSH key, password, or certificate. It should be used instead of hardcoding secrets in Jenkinsfiles, source code, or scripts.

### Q24. What values should not be hardcoded in Jenkinsfiles?

Answer:
Avoid hardcoding:

- secrets
- tokens
- passwords
- production URLs
- registry credentials
- environment-specific values that differ by stage

Safe values to hardcode are stable project identifiers like local app name or manifest path, if appropriate.

### Q25. How would you make one Jenkins pipeline deploy to dev, staging, and prod?

Answer:
Parameterize environment-specific values:

- namespace
- registry repository
- image tag
- approvals
- credentials
- manifest overlays or Helm values

A common pattern is one pipeline with environment-specific stages gated by approvals and protected credentials.

## 5. Docker basics

### Q26. What is Docker?

Answer:
Docker is a container platform that packages an application with its runtime dependencies into an image that can run consistently across environments.

### Q27. What is the difference between a Docker image and a container?

Answer:
- An image is an immutable packaged template.
- A container is a running instance of that image.

### Q28. Why containerize applications in CI/CD?

Answer:
Containers make deployments portable, repeatable, and environment-independent. They reduce “works on my machine” problems and make Kubernetes deployment practical.

### Q29. What does `FROM` do in a Dockerfile?

Answer:
It sets the base image for the new image. Every later instruction builds on top of it.

### Q30. What does `WORKDIR` do?

Answer:
It sets the working directory inside the image/container so later commands run relative to that path.

### Q31. What does `COPY` do?

Answer:
It copies files from the build context on the host into the image filesystem.

### Q32. What does `ENTRYPOINT` do?

Answer:
It defines the main process the container runs when started.

### Q33. Why is a specific Java base image used in this project?

Answer:
Because the project requires Java 21. The image `eclipse-temurin:21-jdk-jammy` provides a matching Java runtime and build compatibility.

### Q34. Why is hardcoding the JAR name in `Dockerfile` risky?

Answer:
If the artifact ID or version changes, the `COPY` command breaks. This is acceptable in a learning project but should be made more flexible in mature pipelines.

### Q35. Why tag Docker images with both `latest` and a unique build tag?

Answer:
- `latest` is convenient for quick local reference
- a unique tag like build number or commit SHA gives traceability and rollback safety

Relying only on `latest` is risky in real delivery systems.

## 6. Docker advanced and scenario questions

### Q36. Why did this project also use fully qualified image tags like `docker.io/library/cicdpractice:7`?

Answer:
Because Kubernetes and Minikube can interpret image names differently depending on runtime and pull behavior. Using a fully qualified name avoids ambiguity and aligns the image loaded into Minikube with the image referenced by the deployment.

### Q37. What is the Docker build context?

Answer:
The build context is the directory sent to Docker during `docker build`. `COPY` instructions can only access files inside this context.

### Q38. What issues can occur if the JAR is not built before Docker build?

Answer:
The `COPY` instruction fails because the expected file under `target/` does not exist. That is why Maven build must complete before Docker image build.

### Q39. How would you optimize this Docker image for production?

Answer:
Common improvements:

- use a smaller runtime image
- use a JRE image instead of JDK if build is done outside image
- use multi-stage builds
- pin base image digests
- add non-root user
- add health endpoints rather than only TCP probes

### Q40. What is the difference between building into Minikube’s daemon and `minikube image load`?

Answer:
- Building directly in Minikube’s Docker daemon makes the image available immediately inside the cluster runtime.
- `minikube image load` copies an already-built local image into Minikube.

This project uses `minikube image load`, which is easier to reason about on Windows.

## 7. Kubernetes basics

### Q41. What is Kubernetes?

Answer:
Kubernetes is a container orchestration platform used to deploy, scale, update, and manage containerized applications across a cluster.

### Q42. What is a Pod?

Answer:
A Pod is the smallest deployable unit in Kubernetes. It contains one or more containers that share networking and storage context.

### Q43. What is a Deployment?

Answer:
A Deployment manages stateless application rollout and desired replica count using ReplicaSets and rolling updates.

### Q44. What is a Service?

Answer:
A Service provides a stable network endpoint to access a set of pods selected by labels.

### Q45. What is a namespace?

Answer:
A namespace is a logical partition inside a Kubernetes cluster used to group and isolate resources.

### Q46. What is a label and selector?

Answer:
Labels are key-value metadata attached to resources. Selectors use those labels to match related resources, for example a Service routing traffic to pods with `app=cicdpractice`.

### Q47. What is `NodePort` service type?

Answer:
It exposes a Service on a port on each node. In this project, the app is internally on `8080` and externally exposed via NodePort `30007`.

### Q48. What is the difference between `port`, `targetPort`, and `nodePort`?

Answer:
- `port`: service port inside the cluster
- `targetPort`: container port receiving traffic
- `nodePort`: port exposed on the Kubernetes node

### Q49. What does `imagePullPolicy: Never` mean?

Answer:
Kubernetes must not try to pull the image from a registry. The image must already be present on the node runtime. This is useful for local Minikube workflows.

### Q50. What are readiness and liveness probes?

Answer:
- Readiness probe decides when a pod is ready to receive traffic.
- Liveness probe decides when a container is unhealthy and should be restarted.

## 8. Kubernetes advanced and scenario questions

### Q51. Why did rollout hang at `1 out of 2 new replicas have been updated...` in this project?

Answer:
Because Kubernetes created a new ReplicaSet, but one of the new pods was stuck in `ErrImageNeverPull`. The rollout waits until the new pod becomes Ready, and if it never does, rollout eventually times out.

### Q52. What does `ErrImageNeverPull` indicate?

Answer:
Kubernetes was instructed not to pull the image, and the requested image was not already present in the node runtime.

### Q53. In this project, why did pods try to run image `6`, `7`, or `8`?

Answer:
Because the PowerShell-based `kubectl set image` command was mangled and Kubernetes received only the tag portion instead of the full image name. This is exactly the sort of cross-shell quoting issue that causes real pipeline failures.

### Q54. How was that fixed?

Answer:
By switching to a safer deployment update method:

- use a fully qualified image name
- verify the deployment image immediately after update
- avoid ambiguous shell expansion

### Q55. Why is `kubectl apply` used before patching the image?

Answer:
`kubectl apply` ensures the deployment and service definitions are present and updated. The image patch is then applied as the dynamic build-specific override.

### Q56. Why use `kubectl rollout status`?

Answer:
It verifies that the new deployment revision has actually become healthy, rather than assuming success after applying manifests.

### Q57. How would you debug a rollout failure?

Answer:
Typical sequence:

1. `kubectl get deploy,rs,pods -n <ns> -o wide`
2. `kubectl describe deployment <name> -n <ns>`
3. `kubectl get events -n <ns> --sort-by=.metadata.creationTimestamp`
4. `kubectl get pod <pod> -n <ns> -o yaml`
5. `kubectl logs <pod> -n <ns>`

### Q58. Why can `NodePort` fail on Windows even when the app is healthy?

Answer:
Because Minikube with Docker driver on Windows may require a local tunnel or special routing behavior. The service can be healthy in-cluster while direct access to `http://<minikube-ip>:30007` is unreliable from the host.

### Q59. Why does `minikube service --url` sometimes return `127.0.0.1:<random-port>`?

Answer:
On Windows with Docker driver, Minikube may create a local proxy tunnel. That is why the port is random and the terminal must stay open.

### Q60. What is the difference between Minikube and production Kubernetes?

Answer:
Minikube is a single-node local development cluster. Production Kubernetes usually has multiple nodes, managed networking, external registries, ingress controllers, autoscaling, RBAC, monitoring, and more formal release controls.

## 9. Minikube-specific interview questions

### Q61. Why use Minikube in a CI/CD demo?

Answer:
Minikube is a lightweight local Kubernetes environment suitable for development, learning, and demonstrating build-to-deploy workflows without requiring cloud infrastructure.

### Q62. What are typical Minikube problems in CI/CD?

Answer:
- wrong context
- profile not started
- runtime mismatch
- image not loaded into Minikube
- local networking differences
- Jenkins service account not seeing Minikube config

### Q63. Why was `minikube update-context -p minikube` needed?

Answer:
Because the Jenkins service account or session may not have the correct kubeconfig context. Updating context ensures `kubectl` talks to the Minikube cluster rather than `localhost:8080` or an invalid API endpoint.

## 10. Scenario-based questions

### Q64. A webhook is successfully reaching Jenkins, but the build still runs the old pipeline. Why?

Answer:
Because Jenkins reads the `Jenkinsfile` from the latest pushed commit in GitHub, not from your uncommitted local workspace. If the file is modified locally but not pushed, Jenkins will continue using the older pipeline.

### Q65. Jenkins says build is successful, but deployment failed. How do you explain that?

Answer:
The CI part and CD part are separate. Application compilation and tests may have succeeded, but deployment can still fail due to image issues, cluster problems, bad manifests, readiness failures, or incorrect kube context.

### Q66. A new image was built successfully, but pods still run the old image. What would you check?

Answer:
I would check:

1. whether the deployment template image actually changed
2. whether the new image tag exists in registry or Minikube
3. whether image pull policy allows retrieval
4. whether rollout started a new ReplicaSet
5. whether the patch command or Helm values were correct

In this project, the practical lesson was that build success and even image build success did not guarantee the service was using the new code. The real check was the deployment image, pod image, and live in-cluster response.

### Q67. A deployment has two old pods running and one new pod pending forever. What does that usually mean?

Answer:
The rolling update has started, but the new pod is failing readiness, image pull, or startup. Kubernetes keeps the old pods available to avoid downtime and waits for the new one to become healthy.

### Q68. Your Jenkins job works manually but not through webhook. What would you check?

Answer:
I would check:

1. GitHub webhook delivery status
2. Jenkins reachable URL
3. job trigger setting
4. Jenkins port configuration
5. ngrok status if using local Jenkins

### Q69. Jenkins can run `mvn`, but cannot run `kubectl`. Why?

Answer:
That usually means the Jenkins runtime has Maven in PATH but not `kubectl`, or it is running under a different user/service account than expected. Preflight validation should expose that early.

### Q70. Why did running Jenkins as a Windows service matter in this project?

Answer:
Because the Jenkins service account determines which PATH, Docker access, kubeconfig, and Minikube profile are visible. Running under the wrong account caused cluster visibility and tool access issues earlier.

### Q71. A deployment manifest contains `image: cicdpractice:latest`, but Jenkins later patches the image. Why keep both?

Answer:
The manifest provides a base/default image, useful for local `kubectl apply` without Jenkins. The pipeline later overrides that image with the current build-specific tag for proper traceability.

### Q72. Why is using only `latest` a bad production practice?

Answer:
Because it is not immutable, makes rollback harder, weakens traceability, and can cause different nodes to resolve different actual images over time. Unique tags or digests are safer.

### Q73. How would you introduce rollback into this pipeline?

Answer:
Options include:

- retain previous image tags
- use `kubectl rollout undo deployment/<name>`
- keep release metadata
- use Git commit SHA tags
- promote tested images instead of rebuilding for each environment

### Q74. How would you improve this pipeline for production?

Answer:
I would add:

- dedicated registry instead of local Minikube image load
- unique immutable image tags like Git SHA
- secrets via Jenkins credentials and Kubernetes Secrets
- dev/stage/prod environments
- approvals for production
- health endpoint-based probes
- linting and security scans
- Helm or Kustomize
- observability and notifications

### Q75. How would you handle zero-downtime deployment in Kubernetes?

Answer:
Use rolling updates with appropriate readiness probes, sufficient replica count, conservative maxUnavailable, and only switch traffic after the new pods are ready.

## 11. Advanced architecture questions

### Q76. Why is `kubectl patch` sometimes safer than `kubectl set image` in scripted pipelines?

Answer:
Because quoting and variable interpolation across shells can break `set image` arguments. A patch can make the intended JSON payload explicit and reduce parsing ambiguity.

Practical note:
- whichever method you use, the key control is verifying the deployment image immediately after the update

### Q77. What is the purpose of immutable image tags?

Answer:
They make each deployment uniquely identifiable and reproducible. This improves rollback, traceability, auditability, and incident analysis.

### Q78. Why is a local Minikube flow not enough to validate a real production-grade pipeline?

Answer:
Because it does not reflect multi-node scheduling, cloud networking, external registries, ingress, secret management, autoscaling, or production reliability constraints.

### Q79. Why should secrets not be stored in Git or `Jenkinsfile`?

Answer:
Because they become visible in history, logs, pull requests, and forks. Secrets should be injected securely from Jenkins credentials, secret stores, or Kubernetes secrets managers.

### Q80. What would you monitor in a mature CI/CD platform?

Answer:
I would monitor:

- build duration
- failure rate by stage
- deployment success rate
- rollback frequency
- lead time for changes
- mean time to recovery
- cluster health
- container restart rates
- probe failures

## 12. Short rapid-fire questions

### Q81. What does `checkout scm` do?

Answer:
It checks out the source code configured in the Jenkins job SCM.

### Q82. What does `mvn clean verify` do?

Answer:
It cleans previous outputs, builds the project, runs tests, and verifies the artifact.

### Q83. What does `kubectl apply` do?

Answer:
It creates or updates Kubernetes resources declaratively from manifest files.

### Q84. What does `kubectl rollout status` do?

Answer:
It waits for a deployment rollout to complete successfully or fail by timeout.

### Q85. What does `minikube image load` do?

Answer:
It copies a local Docker image into the Minikube runtime so Kubernetes can run it without pulling from a registry.

### Q86. What does `imagePullPolicy: Never` require?

Answer:
The image must already exist on the Kubernetes node runtime.

### Q87. What is the main benefit of webhooks over polling?

Answer:
Immediate event-driven triggering with less waste.

### Q88. Why use a build number in image tags?

Answer:
It gives uniqueness and traceability for each Jenkins run.

## 13. Strong 5-year-experience answer style

For 5-year interviews, your answers should sound like this:

- explain the concept clearly
- mention tradeoffs
- mention one real failure mode
- mention how you would debug or improve it

Example:

Question:
- Why did your rollout fail even though Docker build passed?

Strong answer:
- Docker build only proves the image was built locally. It does not prove Kubernetes can access or run that image. In our case, rollout failed because the deployment referenced an incorrect image name and the pod entered `ErrImageNeverPull`. I verified that by checking the pod YAML, deployment events, and Minikube image list. The fix was to use a fully qualified image reference and patch the deployment image explicitly.

## 14. Final preparation advice

For this toolset, be ready to answer at three levels:

1. Concept
   - what the tool does
2. Implementation
   - what commands or config you used
3. Failure analysis
   - what broke, why it broke, and how you fixed it

If you can explain all three levels, you will perform much better than candidates who only memorize definitions.

## 15. HR-style DevOps questions

### Q89. Tell me about yourself as a DevOps or CI/CD engineer.

Answer:
I have around 5 years of experience working across build automation, CI/CD pipelines, containerization, and Kubernetes-based deployments. My focus has been on making delivery reliable, observable, and repeatable. I usually work closely with developers to standardize pipelines, reduce manual deployment work, and improve debugging and rollback processes.

### Q90. Why do you want to work in a DevOps-oriented role?

Answer:
I like working at the point where development speed and production reliability meet. DevOps work has visible business impact because improvements in pipeline quality, deployment reliability, and recovery time directly affect release confidence and engineering productivity.

### Q91. How do you handle production pressure during an incident?

Answer:
I focus on reducing uncertainty quickly. I first confirm impact and scope, then stabilize the system using rollback, traffic control, or service isolation if needed. After that, I work through logs, events, metrics, and recent changes in a structured way. I avoid guessing and I keep communication short and factual.

### Q92. Describe a conflict you had with developers or operations and how you resolved it.

Answer:
A common conflict is speed versus stability. I usually handle it by converting opinions into observable criteria. For example, instead of arguing about whether a release is safe, I define deployment gates such as tests, health checks, rollout success, and rollback conditions. That shifts the conversation from preference to evidence.

### Q93. How do you prioritize when many pipeline issues exist at the same time?

Answer:
I prioritize by production impact first, then deployment blockage, then developer productivity impact. A broken production deployment path is more important than a flaky optional report stage. I also look for problems with the largest blast radius, such as a shared credential issue or broken agent image.

### Q94. What does ownership mean to you in a DevOps role?

Answer:
Ownership means taking responsibility for delivery reliability end to end, not just writing a pipeline script. It includes understanding how code is built, how it is deployed, how it fails, how it is rolled back, and how teams use the system day to day.

### Q95. How do you communicate technical issues to non-technical stakeholders?

Answer:
I translate them into impact, risk, and ETA. For example, instead of saying "the rollout failed due to image resolution mismatch", I would say "the new version did not start in Kubernetes, the old version is still serving traffic, there is no user-facing outage, and we expect a fix in 20 minutes."

### Q96. How do you handle repetitive manual tasks?

Answer:
If a task is repeated and predictable, I treat it as an automation candidate. I first understand the exact decision points, then automate the stable parts, and keep human approval only where risk justifies it.

## 16. System design questions for CI/CD

### Q97. Design a CI/CD pipeline for a microservices-based platform.

Answer:
A good design would include:

1. source control per service with shared standards
2. pull request validation pipeline
3. artifact build and container image creation
4. push to central registry
5. environment promotion strategy
6. deployment through Helm or GitOps
7. post-deploy verification and rollback
8. centralized logging, metrics, and alerting

I would avoid rebuilding the same artifact per environment. I would build once, promote the same immutable image across dev, staging, and production.

### Q98. How would you design rollback for Kubernetes deployments?

Answer:
I would use immutable image tags, deployment revisions, and health-based rollout checks. Rollback options would include `kubectl rollout undo`, redeploying a previous approved image tag, or GitOps revert if manifests are the source of truth. The key is that rollback must be fast and deterministic.

### Q99. How would you design secret management in a CI/CD platform?

Answer:
I would separate build-time and runtime secrets. Build-time secrets should come from Jenkins credentials or a vault system. Runtime secrets should come from a cloud secret manager or Kubernetes-integrated secret solution. Secrets should never be committed to Git or printed in logs.

### Q100. How would you secure a Jenkins-based delivery platform?

Answer:
I would secure:

- authentication and RBAC
- agent isolation
- credentials scoping
- plugin hygiene
- audit logs
- HTTPS
- limited network access
- least-privilege cloud IAM roles
- hardened agent images

I would also reduce long-lived secrets and prefer identity-based access where possible.

### Q101. How would you design deployment strategy for zero downtime?

Answer:
I would choose rolling updates, blue-green, or canary based on risk and traffic patterns. For Kubernetes, rolling updates with readiness probes are the baseline. For higher-risk services, canary with progressive traffic shifting and metrics-based promotion is better.

### Q102. How would you design pipeline scalability for many teams?

Answer:
I would standardize shared pipeline templates, agent images, credential patterns, quality gates, and artifact conventions. Each team can extend the template, but core build, test, and deploy standards remain centralized. This improves consistency without forcing identical repos.

### Q103. How would you reduce CI build time across a large engineering team?

Answer:
I would optimize dependency caching, parallel test execution, selective builds, reusable agent images, and repository structure. I would also measure stage timing so optimization targets are based on evidence rather than assumption.

### Q104. How would you design auditability in a CI/CD system?

Answer:
Every deployment should answer:

1. what was deployed
2. who triggered it
3. from which commit
4. through which pipeline
5. into which environment
6. with what result

That usually means immutable image tags, commit metadata, build numbers, deployment history, and centralized logs.

## 17. Production and AWS-focused questions

### Q105. How would this local Jenkins + Minikube pipeline translate to AWS?

Answer:
The local concepts stay the same, but the implementation changes:

- Minikube becomes EKS
- local Docker images become images pushed to ECR
- local NodePort access becomes Ingress or LoadBalancer
- local Jenkins may remain Jenkins, or CI may move to GitHub Actions, CodeBuild, or another managed service
- secrets move to AWS Secrets Manager or Parameter Store

### Q106. What AWS services are commonly used in a production CI/CD pipeline?

Answer:
Common services include:

- CodeCommit or GitHub for source
- CodeBuild or Jenkins for builds
- ECR for container registry
- EKS or ECS for deployment
- S3 for artifact storage
- CloudWatch for logs and metrics
- IAM for access control
- Secrets Manager or SSM Parameter Store for secrets
- ALB for ingress

### Q107. What is Amazon ECR and why is it important?

Answer:
ECR is AWS’s managed container registry. It stores Docker images securely, supports image scanning, integrates with IAM, and is the normal place to push images before deploying them to EKS or ECS.

### Q108. What is Amazon EKS?

Answer:
EKS is AWS’s managed Kubernetes service. AWS manages the control plane, while you manage worker nodes or use Fargate for serverless compute. It removes the burden of self-managing Kubernetes masters.

### Q109. How would you deploy this app to EKS instead of Minikube?

Answer:
I would:

1. build the image
2. tag it with commit SHA or build number
3. push it to ECR
4. update Kubernetes manifests or Helm values to use the ECR image
5. apply to the EKS cluster
6. monitor rollout and logs

The main difference is that EKS pulls from ECR, so `imagePullPolicy: Never` would no longer be appropriate.

### Q110. What changes are needed in manifests when moving from local Minikube to AWS EKS?

Answer:
Common changes:

- image path changes to ECR URL
- `imagePullPolicy` becomes `IfNotPresent` or `Always`
- service often changes from `NodePort` to `LoadBalancer` or Ingress-backed service
- ingress resources may be introduced
- resource requests and limits become more important
- secrets and configs become environment-specific

### Q111. How would you authenticate Jenkins to AWS securely?

Answer:
Best practice is to avoid long-lived static credentials where possible. Preferred options are:

- IAM role for EC2 if Jenkins runs on EC2
- IRSA for pods if Jenkins or agents run in EKS
- short-lived assumed roles through STS

If static keys must be used, they should be stored only in Jenkins credentials and rotated regularly.

### Q112. How would you design multi-environment deployment on AWS?

Answer:
I would separate dev, staging, and production at least by namespace, and often by account or cluster depending on scale and security needs. Promotion should use the same tested image moving across environments, not rebuilt images.

### Q113. What is IRSA and why is it useful in EKS?

Answer:
IRSA stands for IAM Roles for Service Accounts. It allows Kubernetes service accounts to assume AWS IAM roles without embedding static AWS keys in pods. It is the preferred secure integration pattern for EKS workloads accessing AWS services.

### Q114. How would you store application secrets on AWS for Kubernetes workloads?

Answer:
I would use AWS Secrets Manager or SSM Parameter Store, and integrate them with EKS through External Secrets Operator, CSI drivers, or a controlled secret sync pattern. This centralizes rotation and access control.

### Q115. How would you monitor deployments on AWS?

Answer:
I would use CloudWatch for logs and metrics, Prometheus/Grafana for Kubernetes metrics if needed, ALB target health, pod events, and deployment history. I would correlate build number, image tag, and deployment timestamp to observe regressions quickly.

### Q116. How would you handle blue-green or canary deployment in AWS?

Answer:
Options include:

- Argo Rollouts on EKS
- ALB weighted routing
- service mesh-based traffic shifting
- CodeDeploy for ECS/EKS depending on architecture

The main goal is progressive release with automated health evaluation.

### Q117. What production concerns exist on AWS that do not appear in Minikube?

Answer:
- IAM and cross-account access
- network policies and VPC design
- high availability across AZs
- autoscaling
- registry permissions
- load balancer costs and health checks
- observability at scale
- secret rotation
- disaster recovery

## 18. Real production troubleshooting questions

### Q118. Production deployment succeeded in Jenkins, but users see errors after release. What do you do first?

Answer:
I first confirm the blast radius and whether rollback is needed. Then I check deployment version, pod health, ingress or load balancer health, application logs, and any metrics that changed around release time. If impact is high and confidence is low, I prefer a fast rollback before root-cause deep dive.

### Q119. A deployment works in staging but fails in production. What are likely reasons?

Answer:
Typical causes are:

- different config or secrets
- insufficient permissions
- different traffic volume
- tighter security policies
- different resource limits
- production-only integrations like databases or queues

### Q120. Pods are healthy, but traffic is failing through the load balancer. What would you check?

Answer:
I would check service selectors, endpoints, ingress rules, ALB target group health, security groups, path routing, TLS configuration, and whether readiness probes are aligned with real application readiness.

### Q121. After a deployment, CPU spikes and autoscaling starts thrashing. What do you do?

Answer:
I check whether the new version introduced heavier startup or request cost, whether resource requests are too low, whether autoscaling targets are appropriate, and whether there is a dependency bottleneck. If user impact is visible, I rollback first and analyze second.

### Q122. A new image tag exists in the registry, but Kubernetes keeps running the previous version. Why?

Answer:
Possible reasons:

- deployment image was not updated
- GitOps controller reverted the change
- rollout failed and old ReplicaSet stayed active
- wrong namespace or cluster was targeted
- identical tag reused and image pull policy prevented refresh

### Q123. A Jenkins controller goes down during a production deploy. How do you reduce that risk?

Answer:
Use resilient controller storage, backup strategy, externalized agent execution, and idempotent deployment steps. Critical production deploy systems should not rely on a fragile single-node setup without recovery planning.

### Q124. What metrics would you present to leadership for CI/CD maturity?

Answer:
I would present:

- deployment frequency
- change lead time
- change failure rate
- mean time to recovery
- build success rate
- rollout success rate
- average pipeline duration

These metrics connect engineering process to business delivery outcomes.

## 19. Mock interview rounds with concise spoken answers

### Round 1. Basic screening

Question:
- What is the difference between Continuous Delivery and Continuous Deployment?

Spoken answer:
- Continuous Delivery means every change is validated and ready to release, but production deployment may still need approval. Continuous Deployment goes one step further and pushes every validated change automatically to production.

Question:
- Why do we use Docker in CI/CD?

Spoken answer:
- Docker gives us a consistent runtime package. We build once, then deploy the same image across environments, which reduces environment mismatch issues.

Question:
- What does a Kubernetes Deployment do?

Spoken answer:
- A Deployment manages pod replicas and rolling updates. It creates ReplicaSets and helps us roll out new versions safely.

### Round 2. Mid-level practical round

Question:
- Your Jenkins build passed, but deployment failed with `ErrImageNeverPull`. What does that mean?

Spoken answer:
- It means Kubernetes was told not to pull the image and the requested image was not present on the node runtime. I would verify the deployment image value, check Minikube or registry image availability, and inspect the failed pod YAML and events.

Question:
- Why is `kubectl rollout status` important?

Spoken answer:
- Because applying manifests alone does not prove the release is healthy. `rollout status` validates whether the new revision actually became ready.

Question:
- Why is using `latest` risky?

Spoken answer:
- `latest` is not immutable. It reduces traceability and makes rollback harder because you cannot reliably map a deployment back to a specific build.

### Round 3. Senior practical troubleshooting round

Question:
- A rollout is stuck at `1 out of 2 new replicas have been updated`. How do you debug it?

Spoken answer:
- I check deployment, ReplicaSets, pods, and events first. Then I inspect the failing pod YAML or logs to see whether the issue is image pull, readiness, config, or startup behavior. The rollout controller is usually waiting on one unhealthy new pod.

Question:
- Jenkins is running on Windows and the pipeline behaves differently from Linux. Why?

Spoken answer:
- Shell behavior and quoting differ across PowerShell, batch, and sh. In our case that affected image update commands. Cross-platform pipelines need explicit handling for each shell.

Question:
- How would you productionize this local Minikube-based solution?

Spoken answer:
- I would move images to ECR, deploy to EKS, replace NodePort with Ingress or LoadBalancer, manage secrets through AWS services, use immutable image tags, and add environment promotion plus rollback controls.

### Round 4. HR plus leadership round

Question:
- Tell me about a difficult deployment issue you solved.

Spoken answer:
- I worked through a rollout issue where the build succeeded but Kubernetes could not start the new pod because the deployment referenced the wrong image name. I traced it through Jenkins logs, pod YAML, deployment events, and image inventory. The fix was to make the image reference explicit and patch the deployment safely. The key lesson was that successful build does not guarantee deployability.

Question:
- How do you balance release speed and safety?

Spoken answer:
- I automate everything that is deterministic, but I keep gates where risk is high. The balance comes from strong validation, observability, fast rollback, and environment-specific controls rather than from manual effort everywhere.

## 20. How to answer production cloud questions strongly

A strong answer for cloud production questions usually includes:

1. The target AWS service
2. Security model
3. Deployment flow
4. Failure handling
5. Observability
6. Rollback plan

Example:

Question:
- How would you deploy a containerized application to AWS in production?

Strong answer:
- I would build the image in CI, tag it immutably with the commit SHA or build number, push it to ECR, and deploy it to EKS using Helm or a GitOps workflow. I would use IAM-based authentication, store secrets in Secrets Manager, expose the service through ALB ingress, and validate rollout using health checks and metrics. Rollback would be handled through deployment revision history or by promoting a previously known-good image.
