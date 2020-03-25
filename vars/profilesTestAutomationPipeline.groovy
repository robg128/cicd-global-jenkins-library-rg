/**
 * The Jenkins Test Pipeline.
 *
 * Used to run automated tests for Profiles.
 *
 * Values to provide: serviceName, slackChannel
 *
 * @param body the pipeline parameters
 */
def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    // required pipeline values
    def requiredValues = ["serviceName", "slackChannel"]

    ///////////// Pipeline Definition /////////////
    pipeline {
        agent any

        stages {
            stage("Validate Jenkinsfile") {
                steps {
                    validateJenkinsfile(config, requiredValues)
                }
            }
            stage("Run tests") {
                steps {
                    script {
                        if (env.PULL_REQUEST == "") {
                            env.SUT_URL = "https://${config.serviceName}.${ENV_DOMAIN_FRAGMENT}.monster-next.com"
                        } else {
                            env.SUT_URL = "https://${env.PULL_REQUEST}-${config.serviceName}.${ENV_DOMAIN_FRAGMENT}.monster-next.com"
                        }
                    }
                    container('maven') {
                        configFileProvider([configFile(fileId: 'maven_settings', variable: 'MAVEN_SETTINGS')]) {
                            sh("cp ${MAVEN_SETTINGS} ./maven_settings.xml")
                            sh("echo Running tests for service at url: ${env.SUT_URL}")
                            sh("mvn -s maven_settings.xml --no-transfer-progress -D SUT_URL=${env.SUT_URL} clean install")
                        }
                    }
                }
            }
        }

        // post stages
        post {
            always {
                publishHTML(target: [allowMissing: false, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'target/spock-reports', reportFiles: 'index.html', reportName: "HTMLreport"])
            }
            success {
                slackSend(color: '#4aba00', message: "*SUCCESS:* ${env.PULL_REQUEST} ${JOB_BASE_NAME}  <${BUILD_URL}HTMLreport|Report>", channel: config.slackChannel)
            }
            failure {
                slackSend(color: '#d10202', message: "*FAILURE:* ${env.PULL_REQUEST} ${JOB_BASE_NAME}  <${BUILD_URL}HTMLreport|Report>", channel: config.slackChannel)
            }
        }
    }
}
