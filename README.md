CI/CD Pipeline for Frontend and Backend
This repository contains a Jenkins pipeline configuration for automating the build, test, and deployment process of a frontend and backend application using Docker. The pipeline builds Docker images for both applications, scans them for vulnerabilities, and pushes them to Docker Hub.

Pipeline Overview
The Jenkins pipeline includes the following stages:

Git Checkout:

Retrieves the latest code from the GitHub repository.
List Workspace:

Lists the contents of the workspace and subdirectories to verify the files.
Install Dependencies:

Installs Node.js dependencies for both frontend and backend applications.
Build Applications:

Builds the frontend and backend applications using npm.
SonarQube Analysis:

(Optional) Analyzes the code quality and security with SonarQube.
Build Docker Images:

Creates Docker images for the frontend and backend applications.
Trivy Scanning:

Scans the Docker images for vulnerabilities using Trivy.
Push Docker Images:

Pushes the Docker images to Docker Hub.
Configuration Details
Environment Variables:

docker_frontend_image_name: Name of the Docker image for the frontend application.
docker_backend_image_name: Name of the Docker image for the backend application.
docker_tag: Docker image tag based on the Jenkins build number.
DOCKER_USERNAME: Docker Hub username.
Credentials:

docker_creds1: Docker Hub credentials stored as a secret text in Jenkins.
Pipeline Script Breakdown
Git Checkout:

Uses the git command to pull the latest code from the main branch of the repository.
List Workspace:

Lists files and directories to ensure the correct files are present.
Install Dependencies:

Installs Node.js dependencies for the frontend and backend applications.
Build Applications:

Runs npm install and npm run build to prepare the applications.
SonarQube Analysis:

Analyzes the code with SonarQube (if configured). This stage is optional and can be commented out if not needed.
Build Docker Images:

Uses docker build to create Docker images for both the frontend and backend applications.
Trivy Scanning:

Scans the Docker images for vulnerabilities using Trivy.
Push Docker Images:

Logs in to Docker Hub, tags the images, and pushes them to the Docker Hub repository.
How to Use
Set Up Jenkins:

Ensure Jenkins is configured with the necessary tools and credentials.
Configure Pipeline:

Copy the pipeline script into your Jenkins pipeline job configuration.
Trigger Build:

Start the build process from Jenkins. The pipeline will execute the defined steps automatically.
Monitor and Verify:

Check the Jenkins console output for the status of each step.
Verify that Docker images are pushed to Docker Hub and check for any potential vulnerabilities.
Troubleshooting
Port Conflicts:

Ensure that Docker ports are not in use by other services. Adjust port numbers if needed.
Build Failures:

Review the Jenkins console output for detailed error messages and address any issues accordingly.
Additional Notes
Update the image names, ports, and credentials as required for your specific environment.
The SonarQube Analysis stage is optional and can be disabled if not using SonarQube.