/**
 * The Jenkins Docker Pipeline.
 *
 * Used by applications that build docker images to be deployed into a environment.
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
    def requiredValues = [ "name", "major_version", "minor_version" ]

    // the pipeline defaults
    def defaults = [
        // stage skip parameters
        skipBuild: false,
        skipUnitTests: false,
        skipSonarScan: true, // <- remove this once applications are more well-defined
        skipDockerBuild: false,
        skipDockerPush: false,
        skipHelmPackage: false,
        skipHelmPublish: false,
        skipDockerRepoTag: false,
        skipSlackNotification: false,

        // default build parameters for pipeline
        helmEnvironment: "dev"
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
        options { timestamps ()  }
        stages {

            // validate project info
            stage("Validate Build") { steps {
                validateJenkinsfile (config, requiredValues)
                setBuildLocation (config)
                setBuildDefaults (config)
            }}

            // build project
            stage("Build") { steps {
                buildApplication (config, config.buildCommand)
            }}

            // test project
            stage("Unit Tests") { steps {
                testApplication (config, config.testCommand)
            }}

            // SonarQube scanner
            stage("SonarQube Analysis") { steps {
                sonarScanner (config)
            }}

            // build docker image
            stage("Docker Build") { steps {
                dockerBuild (config, config.dockerBuildCommand)
            }}

            // helm package
            stage("Helm Package") { steps {
                helmPackage (config, config.helmPackageCommand)
            }}

            // helm publish
            stage("Helm Publish") { steps {
                helmPublish (config, config.helmPublishCommand)
            }}

            // push docker image
            stage("Docker Push") { steps {
                dockerPush (config, config.dockerPushCommand)
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
