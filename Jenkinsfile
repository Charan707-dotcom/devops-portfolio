
pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                echo 'GitHub repository checkout successful'
            }
        }

        stage('Build Verification') {
            steps {
                echo 'Portfolio repository files:'
                bat 'dir'
            }
        }

        stage('Success') {
            steps {
                echo 'DevOps Portfolio CI/CD pipeline completed successfully!'
            }
        }
    }
}
