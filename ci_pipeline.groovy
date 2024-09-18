pipeline {
    agent any

    tools {
        nodejs 'nodejs'
    }
    
    environment {
        scanner_home = tool 'sonar-scanner'
        docker_frontend_image_name = 'frontend'
        docker_backend_image_name = 'backend'
        docker_tag = "${BUILD_NUMBER}"
        DOCKER_USERNAME = 'vijaysaw9211' // Set Docker username here
    }

    stages {
        stage('Git Checkout') {
            steps {
                git branch: 'main', changelog: false, poll: false, url: 'https://github.com/vijay-saw/kubernetes-mern-app.git'
            }
        }
        
        stage('List Workspace') {
            steps {
                sh 'ls -la'
                sh 'ls -la frontend'
                sh 'ls -la backend'
            }
        }
        
        stage('Install Frontend Dependencies') {
            steps {
                dir('frontend') {
                    sh 'npm install'
                }
            }
        }
        
        stage('Build Frontend') {
            steps {
                dir('frontend') {
                    sh 'npm run build'
                }
            }
        }
        
        stage('Install Backend Dependencies') {
            steps {
                dir('backend') {
                    sh 'npm install'
                }
            }
        }

        
        stage('SonarQube Analysis') {
            steps {
                withCredentials([string(credentialsId: 'jenkins_localhost', variable: 'SONAR_TOKEN')]) {
                    dir('frontend') {
                        sh '''
                            $scanner_home/bin/sonar-scanner \
                                -Dsonar.host.url=http://localhost:9000 \
                                -Dsonar.login=$SONAR_TOKEN \
                                -Dsonar.projectKey=frontend_project_key \
                                -Dsonar.projectName=frontend_project_name \
                                -Dsonar.sources=src
                        '''
                    }
                    dir('backend') {
                        sh '''
                            $scanner_home/bin/sonar-scanner \
                                -Dsonar.host.url=http://localhost:9000 \
                                -Dsonar.login=$SONAR_TOKEN \
                                -Dsonar.projectKey=backend_project_key \
                                -Dsonar.projectName=backend_project_name \
                                -Dsonar.sources=.
                        '''
                    }
                }
            }
        }
        

        stage('Frontend Build Docker Image') {
            steps {
                dir('frontend') {
                    script {
                        sh '''
                            docker build -t ${docker_frontend_image_name}:${docker_tag} .
                        '''
                    }
                }
            }
        }

        stage('Backend Build Docker Image') {
            steps {
                dir('backend') {
                    script {
                        sh '''
                            docker build -t ${docker_backend_image_name}:${docker_tag} .
                        '''
                    }
                }
            }
        }
        
        stage('Trivy Scanning for Frontend') {
            steps {
                dir('frontend') {
                    script {
                        def scanResult = sh(script: "trivy image ${docker_frontend_image_name}:${docker_tag}", returnStdout: true).trim()
                        echo scanResult
                    }
                }
            }
        }
        
        stage('Trivy Scanning for Backend') {
            steps {
                dir('backend') {
                    script {
                        def scanResult = sh(script: "trivy image ${docker_backend_image_name}:${docker_tag}", returnStdout: true).trim()
                        echo scanResult
                    }
                }
            }
        }

        stage('Docker Push for Frontend into Docker Hub') {
            steps {
                withCredentials([string(credentialsId: 'docker_creds1', variable: 'DOCKER_PASSWORD')]) {
                    script {
                        sh '''
                            echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin
                            docker tag ${docker_frontend_image_name}:${docker_tag} $DOCKER_USERNAME/${docker_frontend_image_name}:${docker_tag}
                            docker push $DOCKER_USERNAME/${docker_frontend_image_name}:${docker_tag}
                        '''
                    }
                }
            }
        }
        
        stage('Docker Push for Backend into Docker Hub') {
            steps {
                withCredentials([string(credentialsId: 'docker_creds1', variable: 'DOCKER_PASSWORD')]) {
                    script {
                        sh '''
                            echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin
                            docker tag ${docker_backend_image_name}:${docker_tag} $DOCKER_USERNAME/${docker_backend_image_name}:${docker_tag}
                            docker push $DOCKER_USERNAME/${docker_backend_image_name}:${docker_tag}
                        '''
                    }
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: '**/dependency-check-report.xml', allowEmptyArchive: true
        }
        success {
            build job: 'cd_pipeline' // Trigger CD Pipeline on successful completion
        }
    }
}
