/**
 * Sets the default build parameters for a build depending on project type.
 *
 * @param config the pipeline config
 */
def call(Map config = [:]) {
    def defaults = [

            // node defaults
            "node": [
                container: "npm",
                preSetupCommands: {
                    configFileProvider([configFile(fileId: "npmrc", variable: "NPMRC")]) {
                        sh("cp $NPMRC ./.npmrc")
                    }
                },
                buildCommand: {
                    sh("npm ci")
                    sh("npm run build")
                },
                testCommand: { sh("npm run test") },
                dockerBuildCommand: { Map ctx ->
                    sh("docker build  --network=host -t ${ctx.dockerRegistry}/${ctx.name}:${ctx.version} .")
                },
                artifactPublishCommand: { Map ctx ->
                    ctx.branchName == "master" ? sh("npm run release") : echo("Cannot publish to NPM from feature branch!")
                }
            ],

            // gradle defaults
            "gradle": [
                container: "gradle",
                preSetupCommands: { /* no pre-setup command */ },
                buildCommand: { sh("./gradlew build") },
                testCommand: { sh("./gradlew test") },
                dockerBuildCommand: { Map ctx ->
                    sh("docker build  --network=host -t ${ctx.dockerRegistry}/${ctx.name}:${ctx.version} .")
                },
                artifactPublishCommand: { Map ctx ->
                    ctx.branchName == "master" ? sh("./gradlew release") : sh("./gradlew publish")
                }
            ],

            // maven defaults
            "maven": [
                container: "maven",
                preSetupCommands: {
                    configFileProvider([configFile(fileId: "maven_settings", variable: "MAVEN_SETTINGS")]) {
                        sh("cp $MAVEN_SETTINGS ./maven_settings.xml")
                    }
                },
                buildCommand: { sh("mvn package --settings ./maven_settings.xml --no-transfer-progress") },
                testCommand: { sh("mvn test --settings ./maven_settings.xml --no-transfer-progress") },
                dockerBuildCommand: { Map ctx ->
                    sh("docker build --build-arg maven_settings=maven_settings.xml --network=host -t ${ctx.dockerRegistry}/${ctx.name}:${ctx.version} .")
                },
                artifactPublishCommand: { Map ctx ->
                    ctx.branchName == "master" ? sh("mvn release:prepare release:perform --settings ./maven_settings.xml --no-transfer-progress") : sh("mvn deploy --settings ./maven_settings.xml --no-transfer-progress")
                }
            ],

            // sbt defaults
            "sbt": [
                container: "sbt",
                preSetupCommands: { },
                buildCommand: { },
                testCommand: { },
                dockerBuildCommand: { },
                artifactPublishCommand: { }
            ],
    ]
    def projectType = getProjectType()

    echo "Detected project type: $projectType"
    config.project = defaults[projectType]

    // additional common variables
    config.branchName = env.BRANCH_NAME
    config.version    = getVersion(config)

    // run pre-setup commands for project type
    if (config.project && config.project.preSetupCommands) {
        config.project.preSetupCommands()
    }
}

/**
 * Validates the build against known project types (i.e. maven, gradle, sbt, node).
 *
 * @return the project type, otherwise empty string
 */
def getProjectType() {
    // NodeJS build
    if (fileExists("package.json")) return "node"
    // Gradle build
    if (fileExists("build.gradle") || fileExists("build.gradle.kts")) return "gradle"
    // Maven build
    if (fileExists("pom.xml")) return "maven"
    // SBT build
    if (fileExists("build.sbt")) return "sbt"
    // set "Unknown" if project type not support
    return "Unknown"
}

/**
 * Sets the version used for artifact tagging.
 *  master branch  -> X.X.X
 *  feature branch -> X.X.X-branch
 *
 * @param config the pipeline config
 * @return the version string
 */
def getVersion(Map config) {
    // set the initial version string
    def version = "${config.major_version}.${config.minor_version}.${BUILD_NUMBER}"

    // if not on master branch, prefix the branch name
    return (env.BRANCH_NAME == "master") ? version : "${version}-${BRANCH_NAME}"
}
