/**
 * A Jenkins Pipeline for Carnival CI
 * Builds and tests carnival and publishes groovyDocs
 * 
 */
pipeline {
    agent any
    parameters {
        //string(name: 'STATUS_EMAIL', defaultValue: 'hwilli@pennmedicine.upenn.edu', description: 'Comma sep list of email addresses that should recieve test status notifications.')
        string(name: 'STATUS_EMAIL', defaultValue: 'hwilli@pennmedicine.upenn.edu, hfree@pennmedicine.upenn.edu', description: 'Comma sep list of email addresses that should recieve test status notifications.')
    }
    options {
        timeout(time: 30, unit: 'MINUTES') 
    }
    stages {
        stage('Setup Workspace') { 
            steps {

                /*git(
                    branch: "master", 
                    url: 'https://github.com/PennTURBO/Drivetrain.git'
                )*/
                checkout scm

                // setup local workspace
                fileExists("turbo_properties.properties.template")
                sh 'cp turbo_properties.properties.template turbo_properties.properties'
                
                dir("drivetrain/") {
                    fileExists("build.sbt.template")
                    sh 'cp build.sbt.template build.sbt'
                }
                dir("drivetrain/project/") {
                    fileExists("build.properties.template")
                    fileExists("plugins.sbt.template")
                    sh 'cp build.properties.template build.properties'
                    sh 'cp plugins.sbt.template plugins.sbt'
                }

                script {
                    withCredentials([usernamePassword(credentialsId: '01f509ad-ca70-4727-93c0-71260af83b11', usernameVariable: 'graphDbUserName', passwordVariable: 'graphDbPassword')]) {
                        sh "sed -i 's/testingUsername = your_username/testingUsername = $graphDbUserName/g' turbo_properties.properties"
                        sh "sed -i 's/testingPassword = your_password/testingPassword = $graphDbPassword/g' turbo_properties.properties"
                        sh "sed -i 's/modelUsername = your_username/modelUsername = $graphDbUserName/g' turbo_properties.properties"
                        sh "sed -i 's/modelPassword = your_password/modelPassword = $graphDbPassword/g' turbo_properties.properties"
                        sh "sed -i 's/testingRepository = your_testing_repo/testingRepository = jenkinsTest_$BRANCH_NAME/g' turbo_properties.properties"
                        sh "sed -i 's/modelRepository = your_model_repo/modelRepository = jenkinsModel_$BRANCH_NAME/g' turbo_properties.properties"
                        sh "sed -i 's/testingServiceURL = http:\\/\\/your_dev_db:7200\\//testingServiceURL = http:\\/\\/turbo-prd-db01.pmacs.upenn.edu:7200\\//g' turbo_properties.properties"
                        sh "sed -i 's/productionServiceURL = http:\\/\\/your_prd_db:7200\\//productionServiceURL = http:\\/\\/turbo-prd-db01.pmacs.upenn.edu:7200\\//g' turbo_properties.properties"
                        sh "sed -i 's/modelServiceURL = http:\\/\\/your_db:7200\\//modelServiceURL = http:\\/\\/turbo-prd-db01.pmacs.upenn.edu:7200\\//g' turbo_properties.properties"
                    }
                }
            } 
        }
        stage('Compile') { 
            steps {
                dir("drivetrain/") {
                    sh 'sbt compile'
                }
            }
        }
        stage('Integration Tests') { 
            steps {
                dir("drivetrain/") {
                    sh 'sbt test'
                }
            }
            post {
                always {
                    junit '**/drivetrain/target/test-reports/*.xml'                    
                }
            }
        }
        stage('Deploy to Dev Server') {
            when {
                branch 'master'
            }
            steps {
                build 'Drivetrain deploy turbo-dev-app02'
            }
        }
        stage('Deploy to Prd Server') {
            when {
                branch 'production'
            }
            steps {
                build 'Drivetrain deploy turbo-prd-app02'
            }
        }
    }
    post {
        failure {
            echo 'Pipeline Failure'
            emailext attachLog: false, 
                compressLog: false,
                subject: 'Job \'${JOB_NAME}\' (${BUILD_NUMBER}) failure',
                body: '''${SCRIPT, template="groovy-html.template"}''', 
                mimeType: 'text/html',
                to: "${params.STATUS_EMAIL}",
                //recipientProviders: [culprits()],
                replyTo: "${params.STATUS_EMAIL}"
        }
        unstable {
            echo 'Pipeline Unstable'
            emailext attachLog: false, 
                compressLog: false,
                subject: 'Job \'${JOB_NAME}\' (${BUILD_NUMBER}) unstable',
                body: '''${SCRIPT, template="groovy-html.template"}''',
                mimeType: 'text/html', 
                to: "${params.STATUS_EMAIL}",
                //recipientProviders: [culprits()],
                replyTo: "${params.STATUS_EMAIL}"
        }
        aborted {
            echo 'Pipeline Aborted or Timeout'
            emailext attachLog: false, 
                compressLog: false,
                subject: 'Job \'${JOB_NAME}\' (${BUILD_NUMBER}) unstable',
                body: '''${SCRIPT, template="groovy-html.template"}''',
                mimeType: 'text/html', 
                to: "${params.STATUS_EMAIL}",
                //recipientProviders: [culprits()],
                replyTo: "${params.STATUS_EMAIL}"
        }
        fixed {
            echo 'Pipeline is back to normal'
            emailext attachLog: false, 
                compressLog: false,
                subject: 'Job \'${JOB_NAME}\' (${BUILD_NUMBER}) is back to normal',
                body: '''${SCRIPT, template="groovy-html.template"}''',
                mimeType: 'text/html', 
                to: "${params.STATUS_EMAIL}",
                //recipientProviders: [culprits()], 
                replyTo: "${params.STATUS_EMAIL}"
        }
    }    
}
