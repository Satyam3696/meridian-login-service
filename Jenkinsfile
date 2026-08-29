pipeline {
    agent { label 'dev-agent' }

    environment {
        S3_BUCKET       = 'meridian-build-artifacts-satyam2026'
        DOCKER_IMAGE    = 'satyam3696/meridian-login-service'
        IMAGE_TAG       = "${env.BUILD_NUMBER}"
        ANSIBLE_DIR     = '/home/ec2-user/ansible-deploy'
        DEV_RDS_URL     = 'jdbc:mysql://insurance-dev-db-1.c9ysii4k2blq.ap-south-1.rds.amazonaws.com:3306/insurancedb'
        QA_RDS_URL      = 'jdbc:mysql://insurance-qa-db-1.c9ysii4k2blq.ap-south-1.rds.amazonaws.com'
        UAT_RDS_URL     = 'jdbc:mysql://uat-db-subnet-group.cl0um82s228o.ap-south-1.rds.amazonaws.com:3306/insurancedb'
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'develop',
                    url: 'https://github.com/Satyam3696/meridian-login-service.git',
                    credentialsId: 'github-creds'
            }
        }

        stage('Build - Maven') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Upload Artifact to S3') {
            steps {
                sh """
                    aws s3 cp target/insurance-login-app.jar \
                        s3://${S3_BUCKET}/builds/insurance-login-app-${IMAGE_TAG}.jar
                """
            }
        }

        stage('Build & Push Docker Image') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKERHUB_USER', passwordVariable: 'DOCKERHUB_PASS')]) {
                    sh """
                        echo "\$DOCKERHUB_PASS" | docker login -u "\$DOCKERHUB_USER" --password-stdin
                        docker build -t ${DOCKER_IMAGE}:${IMAGE_TAG} -t ${DOCKER_IMAGE}:latest .
                        docker push ${DOCKER_IMAGE}:${IMAGE_TAG}
                        docker push ${DOCKER_IMAGE}:latest
                    """
                }
            }
        }

        stage('Deploy to DEV (Auto)') {
            steps {
                withCredentials([
                    string(credentialsId: 'dev-db-password', variable: 'DEV_DB_PASS'),
                    usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKERHUB_USER', passwordVariable: 'DOCKERHUB_PASS')
                ]) {
                    sh """
                        ansible-playbook -i ${ANSIBLE_DIR}/inventory.ini ${ANSIBLE_DIR}/deploy-app.yml \
                            --extra-vars "target_env=dev_servers image_tag=${IMAGE_TAG} dockerhub_user=\$DOCKERHUB_USER dockerhub_password=\$DOCKERHUB_PASS db_password=\$DEV_DB_PASS spring_datasource_url='${DEV_RDS_URL}'"
                    """
                }
            }
        }

        stage('Approval - Promote to QA') {
            steps {
                input message: "Dev build #${IMAGE_TAG} deployed. Promote to QA?", ok: 'Deploy to QA'
            }
        }

        stage('Deploy to QA') {
            steps {
                withCredentials([
                    string(credentialsId: 'qa-db-password', variable: 'QA_DB_PASS'),
                    usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKERHUB_USER', passwordVariable: 'DOCKERHUB_PASS')
                ]) {
                    sh """
                        ansible-playbook -i ${ANSIBLE_DIR}/inventory.ini ${ANSIBLE_DIR}/deploy-app.yml \
                            --extra-vars "target_env=qa_servers image_tag=${IMAGE_TAG} dockerhub_user=\$DOCKERHUB_USER dockerhub_password=\$DOCKERHUB_PASS db_password=\$QA_DB_PASS spring_datasource_url='${QA_RDS_URL}'"
                    """
                }
            }
        }

        stage('Approval - Promote to UAT') {
            steps {
                input message: "QA sign-off done for build #${IMAGE_TAG}. Promote to UAT?", ok: 'Deploy to UAT'
            }
        }

        stage('Deploy to UAT') {
            steps {
                withCredentials([
                    string(credentialsId: 'uat-db-password', variable: 'UAT_DB_PASS'),
                    usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKERHUB_USER', passwordVariable: 'DOCKERHUB_PASS')
                ]) {
                    sh """
                        ansible-playbook -i ${ANSIBLE_DIR}/inventory.ini ${ANSIBLE_DIR}/deploy-app.yml \
                            --extra-vars "target_env=uat_servers image_tag=${IMAGE_TAG} dockerhub_user=\$DOCKERHUB_USER dockerhub_password=\$DOCKERHUB_PASS db_password=\$UAT_DB_PASS spring_datasource_url='${UAT_RDS_URL}'"
                    """
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline completed successfully for build #${IMAGE_TAG}"
        }
        failure {
            echo "Pipeline failed - check logs above"
        }
    }
}

