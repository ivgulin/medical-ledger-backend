def eurekaServer = new Structure(
        "eureka-server",
        "structural/eureka-server/build/Dockerfile",
        "127.0.0.1:8761:8761")
def authService = new Structure(
        "auth-service",
        "structural/auth-service/build/Dockerfile",
        "127.0.0.1:8000:8000")
def zuulGateway = new Structure(
        "zuul-gateway",
        "structural/zuul-gateway/build/Dockerfile",
        "8001:8001")
def documentationService = new Structure(
        "documentation-service",
        "structural/documentation-service/build/Dockerfile",
        "127.0.0.1:8002:8002")
def userService = new Structure(
        "user-service",
        "business/user-service/build/Dockerfile",
        "127.0.0.1:8010:8010")
def selfSovereignIdentityService = new Structure(
        "self-sovereign-identity-service",
        "business/self-sovereign-identity-service/build/Dockerfile",
        "127.0.0.1:8011:8011")
def governmentService = new Structure(
        "government-service",
        "business/fake-government-service/build/Dockerfile",
        "127.0.0.1:8012:8012")

def modules = [
        eurekaServer,
        zuulGateway,
        authService,
        userService,
        selfSovereignIdentityService,
        governmentService,
        documentationService
]

pipeline {
    agent any

    stages {
        stage("maven build") {
            steps {
                println 'stage: maven build'
                withMaven(maven: 'maven3') {
                    sh 'mvn -B -Dmaven.test.skip=true clean package'
                }
            }
        }

        stage('deploy') {
            steps {
                println 'stage: deploy'
                script {
                    def serviceName

                    def removeContainerCommand
                    def removeImageCommand
                    def buildCommand
                    def runCommand

                    String gitChanges = sh(
                            script: 'git diff --name-only HEAD^ HEAD',
                            returnStdout: true
                    ).trim()
                    println gitChanges

                    for (int i = 0; i < modules.size(); i++) {
                        serviceName = modules[i].serviceName
                        // if (gitChanges.contains(serviceName)) {

                        int checkIfContainerExists = sh(
                                script: 'docker ps -f name=' + serviceName + ' | grep -w ' + serviceName + ' | wc -l',
                                returnStdout: true
                        ).trim()

                        if (checkIfContainerExists != 0) {
                            removeContainerCommand = 'docker rm \$(docker stop ' + serviceName + ')'
                            println removeContainerCommand
                            sh removeContainerCommand

                            removeImageCommand = 'docker rmi medical-ledger/' + serviceName
                            println removeImageCommand
                            sh removeImageCommand
                        }

                        buildCommand = 'docker build -t medical-ledger/' + serviceName + ' -f ' + modules[i].dockerPath + ' .'
                        println buildCommand
                        sh buildCommand

                        String volume = " -v /var/ledger:/var/ledger"
                        if (serviceName == "government-service")
                            volume = " -v /var/government:/var/government"

                        String port = modules[i].port
                        println port

                        runCommand = 'docker run -d --name ' + serviceName + volume + ' -p ' + port + ' medical-ledger/' + serviceName
                        println runCommand
                        sh runCommand
                        // }
                    }
                }
            }
        }
    }
}

class Structure {
    String serviceName
    String dockerPath
    String port

    Structure(String serviceName, String dockerPath, String port) {
        this.dockerPath = dockerPath
        this.serviceName = serviceName
        this.port = port
    }
}