pipeline {

    agent any

    environment {

        GIT_REPO     = "https://github.com/sairamraavi/jenkins-practice.git"

        SSH_CRED_ID  = "sairam-b16a-aws"

        EC2_USERNAME = "ubuntu"
        EC2_HOST     = "13.127.79.58"

        DOCKER_IMAGE = "sairamraavi/jenkins-practice"
        DOCKER_TAG   = "latest"

        DOCKER_CRED  = "sairam_docker_cred"
    }

    stages {

        stage('Git Checkout') {

            steps {

                git(
                    branch: 'main',
                    url: "${env.GIT_REPO}"
                )

            }
        }

        stage('Verify Workspace') {

            steps {

                sh """
                    pwd
                    ls -al
                """

            }
        }

        stage('Copy files to EC2') {

            steps {

                sshagent(credentials: ["${env.SSH_CRED_ID}"]) {

                    sh """
                        scp -o StrictHostKeyChecking=no -r ./* \
                        ${env.EC2_USERNAME}@${env.EC2_HOST}:/tmp/
                    """

                }
            }
        }

        stage('Move files to /var/www/html') {

            steps {

                sshagent(credentials: ["${env.SSH_CRED_ID}"]) {

                    sh """
                        ssh -o StrictHostKeyChecking=no \
                        ${env.EC2_USERNAME}@${env.EC2_HOST} "

                            sudo rm -rf /var/www/html/*

                            sudo cp -r /tmp/* /var/www/html/

                            sudo chmod -R 755 /var/www/html

                            ls -al /var/www/html

                        "
                    """

                }
            }
        }

        stage('Build Docker Image in EC2') {

            steps {

                sshagent(credentials: ["${env.SSH_CRED_ID}"]) {

                    sh """
                        ssh -o StrictHostKeyChecking=no \
                        ${env.EC2_USERNAME}@${env.EC2_HOST} "

                            cd /var/www/html && \
                            docker build -t ${env.DOCKER_IMAGE}:${env.DOCKER_TAG} .

                        "
                    """

                }
            }
        }

        stage('Docker Login in EC2') {

            steps {

                withCredentials([
                    usernamePassword(
                        credentialsId: "${env.DOCKER_CRED}",
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )
                ]) {

                    sshagent(credentials: ["${env.SSH_CRED_ID}"]) {

                        sh """
                            ssh -o StrictHostKeyChecking=no \
                            ${env.EC2_USERNAME}@${env.EC2_HOST} "

                                echo '${DOCKER_PASS}' | docker login \
                                -u '${DOCKER_USER}' --password-stdin

                            "
                        """

                    }
                }
            }
        }

        stage('Push Docker Image from EC2') {

            steps {

                sshagent(credentials: ["${env.SSH_CRED_ID}"]) {

                    sh """
                        ssh -o StrictHostKeyChecking=no \
                        ${env.EC2_USERNAME}@${env.EC2_HOST} "

                            docker push ${env.DOCKER_IMAGE}:${env.DOCKER_TAG}

                        "
                    """

                }
            }
        }

    }

}
