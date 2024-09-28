pipeline {
    agent any
    environment {
        KUBECONFIG = '/home/vijayansible/.kube/config'  // Path to your Minikube kubeconfig file
    }
    stages {
         stage('git checkout') {
            steps {
                git branch: 'main', changelog: false, poll: false, url: 'https://github.com/vijay-saw/kubernetes-mern-app'
            }
        }

        stage('Deploy Frontend to Kubernetes') {
            steps {
                script {
                    sh 'kubectl apply -f frontend/kubernetes_file/deployment.yaml'
                    sh 'kubectl apply -f frontend/kubernetes_file/service.yaml'
                }
            }
        }

        stage('Deploy Backend to Kubernetes') {
            steps {
                script {
                    sh 'kubectl apply -f backend/kubernetes_file/deployment.yaml'
                    sh 'kubectl apply -f backend/kubernetes_file/service.yaml'
                }
            }
        }
    }
}
