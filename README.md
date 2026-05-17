# Jenkins Practice Application

A simple web application containerized with Docker, designed for Jenkins CI/CD pipeline learning and practice.

## 📋 Table of Contents
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Build Instructions](#build-instructions)
- [Docker Commands](#docker-commands)
- [Jenkins Integration](#jenkins-integration)
- [Troubleshooting](#troubleshooting)

---

## 📁 Project Structure

```
jenkins-practice/
├── index.html          # Sample web application
├── Dockerfile          # Docker container configuration
├── nginx.conf          # Nginx web server configuration
└── README.md           # This file
```

---

## 🔧 Prerequisites

Make sure you have the following installed:

- **Docker** (v20.10 or higher)
  ```bash
  docker --version
  ```
- **Docker Compose** (optional, for multi-container setups)
  ```bash
  docker-compose --version
  ```
- **Git** (for version control)
  ```bash
  git --version
  ```

### Install Docker (if not already installed)

**macOS:**
```bash
brew install docker
# or install Docker Desktop from https://www.docker.com/products/docker-desktop
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt-get update
sudo apt-get install -y docker.io docker-compose
sudo usermod -aG docker $USER
newgrp docker
```

**Windows:**
- Download and install [Docker Desktop for Windows](https://www.docker.com/products/docker-desktop)

---

## 🚀 Quick Start

### 1. Clone the Repository
```bash
git clone https://github.com/sairamraavi/jenkins-practice.git
cd jenkins-practice
```

### 2. Build Docker Image
```bash
docker build -t jenkins-practice:1.0 .
```

### 3. Run Container
```bash
docker run -d -p 8080:80 --name jenkins-practice-app jenkins-practice:1.0
```

### 4. Access Application
Open your browser and navigate to:
```
http://localhost:8080
```

### 5. Stop Container
```bash
docker stop jenkins-practice-app
docker rm jenkins-practice-app
```

---

## 🏗️ Build Instructions

### Step 1: Build the Docker Image

```bash
# Build with a specific tag
docker build -t jenkins-practice:1.0 .

# Build with multiple tags
docker build -t jenkins-practice:1.0 -t jenkins-practice:latest .

# Build with build arguments (for multi-stage builds)
docker build --build-arg BUILD_DATE=$(date -u +'%Y-%m-%dT%H:%M:%SZ') -t jenkins-practice:1.0 .
```

### Step 2: Verify Image Creation

```bash
# List all Docker images
docker images | grep jenkins-practice

# Inspect image details
docker image inspect jenkins-practice:1.0
```

### Step 3: Tag Image for Registry

```bash
# Tag for Docker Hub
docker tag jenkins-practice:1.0 yourusername/jenkins-practice:1.0

# Tag for private registry
docker tag jenkins-practice:1.0 your-registry.com/jenkins-practice:1.0
```

### Step 4: Push to Docker Registry

```bash
# Login to Docker Hub
docker login

# Push to Docker Hub
docker push yourusername/jenkins-practice:1.0

# Push to private registry
docker push your-registry.com/jenkins-practice:1.0
```

---

## 🐳 Docker Commands

### Building

```bash
# Simple build
docker build -t jenkins-practice:1.0 .

# Build with no cache (rebuild all layers)
docker build --no-cache -t jenkins-practice:1.0 .

# Build with specific Dockerfile
docker build -f Dockerfile -t jenkins-practice:1.0 .

# Build and display build output
docker build -t jenkins-practice:1.0 --progress=plain .
```

### Running

```bash
# Run container in background
docker run -d -p 8080:80 --name jenkins-practice-app jenkins-practice:1.0

# Run container with environment variables
docker run -d -p 8080:80 --name jenkins-practice-app \
  -e ENV=production \
  -e LOG_LEVEL=info \
  jenkins-practice:1.0

# Run container with volume mount
docker run -d -p 8080:80 --name jenkins-practice-app \
  -v $(pwd)/logs:/var/log/nginx \
  jenkins-practice:1.0

# Run container in foreground (see logs)
docker run -p 8080:80 jenkins-practice:1.0

# Run container with resource limits
docker run -d -p 8080:80 --name jenkins-practice-app \
  --memory="512m" \
  --cpus="0.5" \
  jenkins-practice:1.0
```

### Managing Containers

```bash
# List running containers
docker ps

# List all containers (including stopped)
docker ps -a

# View container logs
docker logs jenkins-practice-app

# View real-time logs
docker logs -f jenkins-practice-app

# Execute command in container
docker exec -it jenkins-practice-app /bin/sh

# Stop container
docker stop jenkins-practice-app

# Start stopped container
docker start jenkins-practice-app

# Remove container
docker rm jenkins-practice-app

# Restart container
docker restart jenkins-practice-app
```

### Testing

```bash
# Health check
curl http://localhost:8080/health

# Full application test
curl http://localhost:8080

# Check container health status
docker inspect --format='{{.State.Health}}' jenkins-practice-app
```

---

## 🔗 Jenkins Integration

### Declarative Pipeline Example

```groovy
pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = 'docker.io'
        DOCKER_IMAGE = 'yourusername/jenkins-practice'
        DOCKER_TAG = "${BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    sh '''
                        docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} .
                        docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest
                    '''
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    sh '''
                        docker run --rm ${DOCKER_IMAGE}:${DOCKER_TAG} \
                            wget --quiet --spider http://localhost/health
                    '''
                }
            }
        }

        stage('Push to Registry') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'docker-hub', 
                                     usernameVariable: 'DOCKER_USER', 
                                     passwordVariable: 'DOCKER_PASS')]) {
                        sh '''
                            echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin
                            docker push ${DOCKER_IMAGE}:${DOCKER_TAG}
                            docker push ${DOCKER_IMAGE}:latest
                            docker logout
                        '''
                    }
                }
            }
        }

        stage('Deploy') {
            steps {
                script {
                    sh '''
                        docker stop jenkins-practice-app || true
                        docker rm jenkins-practice-app || true
                        docker run -d -p 8080:80 \
                            --name jenkins-practice-app \
                            ${DOCKER_IMAGE}:${DOCKER_TAG}
                    '''
                }
            }
        }
    }

    post {
        always {
            sh 'docker logout || true'
        }
        success {
            echo 'Pipeline executed successfully!'
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}
```

### Scripted Pipeline Example

```groovy
node {
    def dockerImage = "yourusername/jenkins-practice:${BUILD_NUMBER}"

    try {
        stage('Checkout') {
            checkout scm
        }

        stage('Build') {
            sh "docker build -t ${dockerImage} ."
        }

        stage('Test') {
            sh "docker run --rm ${dockerImage} nginx -t"
        }

        stage('Push') {
            withCredentials([usernamePassword(credentialsId: 'docker-hub',
                           usernameVariable: 'USER', passwordVariable: 'PASS')]) {
                sh '''
                    echo $PASS | docker login -u $USER --password-stdin
                    docker push ${dockerImage}
                '''
            }
        }

        stage('Deploy') {
            sh '''
                docker stop jenkins-practice-app || true
                docker run -d -p 8080:80 --name jenkins-practice-app ${dockerImage}
            '''
        }
    }
    catch (Exception e) {
        echo "Error: ${e.message}"
        throw e
    }
}
```

---

## 📊 Dockerfile Explanation

```dockerfile
# Multi-stage build for optimized image
FROM nginx:alpine AS builder
```
- Uses `nginx:alpine` as base (lightweight Alpine Linux)
- `AS builder` creates a named build stage

```dockerfile
COPY index.html /usr/share/nginx/html/
COPY nginx.conf /etc/nginx/nginx.conf
```
- Copies application files into the builder stage

```dockerfile
FROM nginx:alpine
```
- Final stage starts fresh, reducing image size

```dockerfile
COPY --from=builder ...
```
- Copies only necessary files from builder stage

```dockerfile
HEALTHCHECK --interval=30s --timeout=3s ...
```
- Adds health check endpoint for monitoring

```dockerfile
EXPOSE 80
```
- Documents that the container listens on port 80

```dockerfile
CMD ["nginx", "-g", "daemon off;"]
```
- Runs nginx in foreground (required for containers)

---

## 🛠️ Troubleshooting

### Issue: Port Already in Use

```bash
# Find process using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>

# Or use a different port
docker run -d -p 8081:80 --name jenkins-practice-app jenkins-practice:1.0
```

### Issue: Permission Denied (Docker Commands)

```bash
# Add user to docker group (Linux)
sudo usermod -aG docker $USER
newgrp docker

# Verify
docker ps
```

### Issue: Docker Image Not Found

```bash
# Check if image exists
docker images | grep jenkins-practice

# Build the image
docker build -t jenkins-practice:1.0 .

# List all images
docker images
```

### Issue: Container Won't Start

```bash
# Check logs
docker logs jenkins-practice-app

# Inspect container
docker inspect jenkins-practice-app

# Test Dockerfile syntax
docker build -t jenkins-practice:test . --dry-run
```

### Issue: Can't Access Application

```bash
# Check if container is running
docker ps

# Check port mapping
docker port jenkins-practice-app

# Test connectivity
curl http://localhost:8080

# Check health
docker exec jenkins-practice-app wget --spider http://localhost/health
```

---

## 📚 Additional Resources

- [Docker Documentation](https://docs.docker.com/)
- [Nginx Documentation](https://nginx.org/en/docs/)
- [Jenkins Documentation](https://www.jenkins.io/doc/)
- [Docker Best Practices](https://docs.docker.com/develop/dev-best-practices/)

---

## 📝 Notes for Jenkins Integration

1. **Docker-in-Docker (DinD)**: If running Jenkins in Docker, you'll need to mount the Docker socket:
   ```bash
   docker run -v /var/run/docker.sock:/var/run/docker.sock jenkins:latest
   ```

2. **Registry Credentials**: Store Docker registry credentials in Jenkins using the Credentials plugin

3. **Build Context**: Jenkins should have access to the Dockerfile and all necessary files

4. **Push Timing**: Only push to registry on successful builds

---

## 🎯 Next Steps

1. Set up a Jenkinsfile in the repository root
2. Create a Jenkins pipeline job pointing to this repo
3. Configure webhooks for automatic builds on Git push
4. Set up Docker registry credentials in Jenkins
5. Deploy to a test environment

---

## 📄 License

MIT License - Feel free to use this for learning and practice.

---

**Last Updated**: May 2026
**Maintainer**: Jenkins Practice Team
