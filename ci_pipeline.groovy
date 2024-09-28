pipeline {
    agent any

    tools {
        nodejs 'nodejs'
    }

    environment {
        dependency_check_tool = tool 'DP' // Ensure this matches the tool name
        scanner_home = tool 'sonar-scanner'
        frontend_image_name = 'frontend_app'
        backend_image_name = 'backend_app'
        docker_tag = "${env.BUILD_NUMBER}" // Using Groovy syntax for environment variable
        DOCKER_USERNAME = 'vijaysaw9211'
    }

    stages {
        stage('git checkout') {
            steps {
                git branch: 'main', changelog: false, poll: false, url: 'https://github.com/vijay-saw/kubernetes-mern-app'
            }
        }

        stage('listing all files and running npm commands') {
            steps {
                script {
                    sh '''
                    ls -la /
                    ls -la frontend/
                    ls -la backend/
                    '''

                    dir('frontend') {
                        if (!fileExists('node_modules')) {
                            sh 'npm install'
                        } else {
                            echo 'Skipping npm install as node_modules already exists'
                        }

                        if (!fileExists('build')) {
                            sh 'npm run build'
                        } else {
                            echo 'Skipping build as it already exists'
                        }
                    }

                    dir('backend') {
                        if (!fileExists('node_modules')) {
                            sh 'npm install'
                        } else {
                            echo 'Skipping npm install as node_modules already exists'
                        }
                    }
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
                                -Dsonar.sources=.
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

        stage('OWASP Dependency Check for frontend') {
            steps {
                dir('frontend') {
                    sh '''
                    ${dependency_check_tool}/bin/dependency-check.sh \
                    --project "Frontend" \
                    --scan ./ \
                    --format "XML" \
                    --out frontend-dependency-report
                    '''
                }
                dependencyCheckPublisher pattern: 'frontend-dependency-report/dependency-check-report.xml'
            }
        }

        stage('OWASP Dependency Check for backend') {
            steps {
                dir('backend') {
                    sh '''
                    ${dependency_check_tool}/bin/dependency-check.sh \
                    --project "Backend" \
                    --scan ./ \
                    --format "XML" \
                    --out backend-dependency-report
                    '''
                }
                dependencyCheckPublisher pattern: 'backend-dependency-report/dependency-check-report.xml'
            }
        }

        stage('docker build for frontend') {
            steps {
                dir('frontend') {
                    script {
                        sh '''
                        docker build -t ${frontend_image_name}:${docker_tag} . 
                        '''
                    }
                }
            }
        }

        stage('docker build for backend') {
            steps {
                dir('backend') {
                    script {
                        sh '''
                        docker build -t ${backend_image_name}:${docker_tag} . 
                        '''
                    }
                }
            }
        }

        stage('Trivy Scanning for Frontend image') {
            steps {
                dir('frontend') {
                    script {
                        def scanResult = sh(script: "trivy image ${frontend_image_name}:${docker_tag}", returnStdout: true).trim()
                        echo scanResult
                    }
                }
            }
        }

        stage('Trivy Scanning for Backend image') {
            steps {
                dir('backend') {
                    script {
                        def scanResult = sh(script: "trivy image ${backend_image_name}:${docker_tag}", returnStdout: true).trim()
                        echo scanResult
                    }
                }
            }
        }

        stage('frontend to push in docker hub') {
            steps {
                withCredentials([string(credentialsId: 'docker_creds1', variable: 'DOCKER_PASSWORD')]) {
                    script {
                        sh '''
                        docker login -u ${DOCKER_USERNAME} -p $DOCKER_PASSWORD
                        docker tag ${frontend_image_name}:${docker_tag} ${DOCKER_USERNAME}/${frontend_image_name}:${docker_tag}
                        docker push ${DOCKER_USERNAME}/${frontend_image_name}:${docker_tag}
                        '''
                    }
                }
            }
        }

        stage('backend to push in docker hub') {
            steps {
                withCredentials([string(credentialsId: 'docker_creds1', variable: 'DOCKER_PASSWORD')]) {
                    script {
                        sh '''
                        docker login -u ${DOCKER_USERNAME} -p $DOCKER_PASSWORD
                        docker tag ${backend_image_name}:${docker_tag} ${DOCKER_USERNAME}/${backend_image_name}:${docker_tag}
                        docker push ${DOCKER_USERNAME}/${backend_image_name}:${docker_tag}
                        '''
                    }
                }
            }
        }

        stage('Update Deployment YAML for Frontend') {
            steps {
                dir('frontend/kubernetes_file') {
                    script {
                        def new_frontend_image = "${DOCKER_USERNAME}/${frontend_image_name}:${docker_tag}"
                        sh """
                        sed -i "s|image: .*frontend.*|image: ${new_frontend_image}|" deployment.yaml
                        """
                    }
                }
            }
        }

        stage('Update Deployment YAML for Backend') {
            steps {
                dir('backend/kubernetes_file') {
                    script {
                        def new_backend_image = "${DOCKER_USERNAME}/${backend_image_name}:${docker_tag}"
                        sh """
                        sed -i "s|image: .*backend.*|image: ${new_backend_image}|" deployment.yaml
                        """
                    }
                }
            }
        }

        stage('Push Changes to GitHub for Frontend') {
            steps {
                dir('frontend/kubernetes_file') {
                    script {
                        sh '''
                        git config user.email "vijaysaw9211@gmail.com"
                        git config user.name "vijay-saw"
                        git add deployment.yaml
                        git commit -m "Update frontend deployment.yaml with new image tag"
                        '''

                        withCredentials([usernamePassword(credentialsId: 'github_username_password', usernameVariable: 'GITHUB_USERNAME', passwordVariable: 'GITHUB_TOKEN')]) {
                            sh '''
                            git push https://$GITHUB_USERNAME:$GITHUB_TOKEN@github.com/vijay-saw/kubernetes-mern-app.git main
                            '''
                        }
                    }
                }
            }
        }

        stage('Push Changes to GitHub for Backend') {
            steps {
                dir('backend/kubernetes_file') {
                    script {
                        sh '''
                        git config user.email "vijaysaw9211@gmail.com"
                        git config user.name "vijay-saw"
                        git add deployment.yaml
                        git commit -m "Update backend deployment.yaml with new image tag"
                        '''

                        withCredentials([usernamePassword(credentialsId: 'github_username_password', usernameVariable: 'GITHUB_USERNAME', passwordVariable: 'GITHUB_TOKEN')]) {
                            sh '''
                            git push https://$GITHUB_USERNAME:$GITHUB_TOKEN@github.com/vijay-saw/kubernetes-mern-app.git main
                            '''
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            script {
                build job: 'kubernetes_cdpart'
            }
        }
    }
}
