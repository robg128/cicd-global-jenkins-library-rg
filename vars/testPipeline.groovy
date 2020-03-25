/**
 * The Jenkins Test Pipeline.
 *
 * Used to run tests in applications.
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
    def requiredValues = []

    // the pipeline defaults
    def defaults = [
        // stage skip parameters
        skipBuild: false,
        skipUnitTests: false
    ]
    // set the defaults on the pipeline if not already defined
    defaults.each { key, defaultValue ->
        if (!config.containsKey(key)) config[key] = defaultValue
    }

    ///////////// Pipeline Definition /////////////

    pipeline {
        agent {
            kubernetes {
                defaultContainer 'jnlp'
                yaml libraryResource ('podtemplates/default.yaml')
            }
        }
        options { timestamps () }
        stages {

            // validate project info
            stage("Validate Build") { steps {
                validateJenkinsfile (config, requiredValues)
                setBuildDefaults (config)
            }}

            // build project
            stage("Build") { steps {
                buildApplication (config, config.buildCommand)
            }}

            // test project
            stage("Tests") { steps {
                testApplication (config, config.testCommand)
            }}

            // publish test reports
            stage("Publish Test Reports") { steps {
                runStep (config, config.publishTestReportCommand)
            }}
        }

        // post stages
        post {
            always {
                script {
                    // slack notification
                    slackNotifier (config, currentBuild)
                }
            }
        }
    }
}
