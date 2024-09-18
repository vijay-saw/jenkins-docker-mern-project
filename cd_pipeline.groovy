pipeline {
    agent any

    environment {
        docker_frontend_image_name = 'frontend'
        docker_backend_image_name = 'backend'
        docker_tag = "${BUILD_NUMBER}"      // Ensure this matches with CI
        docker_frontend_port = '80'
        docker_backend_port = '5000'
    }

    stages {
        stage('Stop and Remove Existing Frontend Container') {
            steps {
                script {
                    sh '''
                        if [ $(docker ps -q -f name=myfrontendapp) ]; then
                            docker stop myfrontendapp
                            docker rm myfrontendapp
                        fi
                    '''
                }
            }
        }

        stage('Run Frontend Docker Container') {
            steps {
                script {
                    sh '''
                        docker run -d -p ${docker_frontend_port}:80 --name myfrontendapp ${docker_frontend_image_name}:${docker_tag}
                    '''
                }
            }
        }

        stage('Stop and Remove Existing Backend Container') {
            steps {
                script {
                    sh '''
                        if [ $(docker ps -q -f name=mybackendapp) ]; then
                            docker stop mybackendapp
                            docker rm mybackendapp
                        fi
                    '''
                }
            }
        }

        stage('Run Backend Docker Container') {
            steps {
                script {
                    sh '''
                        docker run -d -p ${docker_backend_port}:5000 --name mybackendapp ${docker_backend_image_name}:${docker_tag}
                    '''
                }
            }
        }
    }
}
