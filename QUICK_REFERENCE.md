# Jenkins Practice - Quick Reference Guide

## 🚀 Quick Commands

### Build and Run Locally
```bash
# Build image
docker build -t jenkins-practice:1.0 .

# Run container
docker run -d -p 8080:80 --name jenkins-practice-app jenkins-practice:1.0

# Check if running
docker ps

# View logs
docker logs -f jenkins-practice-app

# Stop container
docker stop jenkins-practice-app

# Remove container
docker rm jenkins-practice-app
```

### Docker Registry
```bash
# Tag for registry
docker tag jenkins-practice:1.0 yourusername/jenkins-practice:1.0

# Login to Docker Hub
docker login

# Push to registry
docker push yourusername/jenkins-practice:1.0

# Pull from registry
docker pull yourusername/jenkins-practice:1.0
```

### Jenkins Integration
```bash
# Create Jenkins credentials for Docker Hub
# In Jenkins UI: Manage Jenkins > Manage Credentials > Add Credentials

# Use in Jenkinsfile:
withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials',
                 usernameVariable: 'USER', passwordVariable: 'PASS')]) {
    sh 'docker login -u $USER -p $PASS'
}
```

## 📊 Useful Commands for Debugging

### Check Image Details
```bash
# List all images
docker images

# Get image ID
docker images -q jenkins-practice

# Inspect image
docker image inspect jenkins-practice:1.0

# Show image layers
docker history jenkins-practice:1.0
```

### Container Management
```bash
# List all containers
docker ps -a

# Get container ID
docker ps -q

# View container logs (last 100 lines)
docker logs --tail 100 jenkins-practice-app

# View real-time logs
docker logs -f jenkins-practice-app

# Execute command in container
docker exec -it jenkins-practice-app /bin/sh

# Get container stats
docker stats jenkins-practice-app

# Inspect container
docker inspect jenkins-practice-app
```

### Network & Port Testing
```bash
# Check port mapping
docker port jenkins-practice-app

# List network interfaces
docker network ls

# Inspect network
docker network inspect bridge

# Test application
curl http://localhost:8080
curl http://localhost:8080/health
```

### Cleanup
```bash
# Remove stopped containers
docker container prune

# Remove dangling images
docker image prune

# Remove unused images
docker image prune -a

# Remove everything (⚠️ use with caution)
docker system prune -a
```

## 🔍 Environment Variables in Dockerfile

```bash
# Build with environment variable
docker build --build-arg BUILD_DATE=$(date -u +'%Y-%m-%dT%H:%M:%SZ') \
  -t jenkins-practice:1.0 .

# Run with environment variable
docker run -d -p 8080:80 \
  -e NODE_ENV=production \
  -e LOG_LEVEL=info \
  jenkins-practice:1.0
```

## 📈 Jenkins Pipeline Snippets

### Declarative Pipeline - Basic
```groovy
pipeline {
    agent any
    
    stages {
        stage('Build') {
            steps {
                sh 'docker build -t jenkins-practice:${BUILD_NUMBER} .'
            }
        }
    }
}
```

### Scripted Pipeline - Basic
```groovy
node {
    stage('Build') {
        sh 'docker build -t jenkins-practice:${BUILD_NUMBER} .'
    }
}
```

### With Error Handling
```groovy
pipeline {
    agent any
    
    stages {
        stage('Build') {
            steps {
                script {
                    try {
                        sh 'docker build -t jenkins-practice:${BUILD_NUMBER} .'
                    } catch (Exception e) {
                        echo "Build failed: ${e.message}"
                        currentBuild.result = 'FAILURE'
                    }
                }
            }
        }
    }
}
```

### With Conditional Execution
```groovy
pipeline {
    agent any
    
    stages {
        stage('Deploy') {
            when {
                branch 'main'
            }
            steps {
                sh 'docker push yourregistry/jenkins-practice:${BUILD_NUMBER}'
            }
        }
    }
}
```

## 🛡️ Security Best Practices

### Don't commit secrets
```bash
# Add to .gitignore
echo ".env" >> .gitignore
echo "secrets/" >> .gitignore
```

### Use Docker secrets (in Swarm)
```bash
docker secret create db_password -
```

### Use environment files
```bash
docker run --env-file .env.production jenkins-practice:1.0
```

### Don't run as root
```dockerfile
USER app:app
```

### Scan images for vulnerabilities
```bash
docker scan jenkins-practice:1.0
```

## 📝 Git Workflow

```bash
# Clone repository
git clone https://github.com/sairamraavi/jenkins-practice.git
cd jenkins-practice

# Create feature branch
git checkout -b feature/my-feature

# Make changes
git add .
git commit -m "Add new feature"

# Push to remote
git push origin feature/my-feature

# Create Pull Request (on GitHub)
```

## 🔗 Useful Links

- Docker Docs: https://docs.docker.com/
- Jenkins Docs: https://www.jenkins.io/doc/
- Docker Hub: https://hub.docker.com/
- Nginx Docs: https://nginx.org/en/docs/
- Dockerfile Reference: https://docs.docker.com/engine/reference/builder/

---

## 💡 Tips & Tricks

1. **Use `.dockerignore`** - Exclude unnecessary files to reduce build time
2. **Layer caching** - Order Dockerfile commands from least to most frequently changed
3. **Multi-stage builds** - Reduce final image size by using multiple build stages
4. **Health checks** - Add health checks to monitor container status
5. **Volume mounts** - Mount directories for persistence or live reloading
6. **Tags** - Use meaningful tags for version control
7. **Build args** - Make Dockerfile configurable with build arguments
8. **Jenkins credentials** - Store secrets securely in Jenkins, not in code

---

**Last Updated**: May 2026
