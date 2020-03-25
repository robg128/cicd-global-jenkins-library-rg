/**
 * Builds a docker container for the application.
 *
 * Developers can override the the build command by setting the following in the Jenkinsfile:
 *
 *   skipDockerBuild    = false
 *   dockerBuildCommand = { code... }
 *
 * @param config             the pipeline config
 * @param dockerBuildCommand the provided docker build command
 */
def call(Map config = [:], Closure dockerBuildCommand) {
    // check if we are skipping build stage
    if (config.skipDockerBuild == true) {
        echo "Skipping docker build stage..."
    } else {
        // check if docker branch regex has been set otherwise build images on all branches
        def dockerBranchRegex = config.dockerBranchRegex ?: ".*"
        if (!(env.BRANCH_NAME ==~ /$dockerBranchRegex/)) {
            echo "Skipping docker build stage as ${BRANCH_NAME} does not match regex /${dockerBranchRegex}/ ..."
        } else {
            // add 'dockerTag' and 'dockerImage' to pipeline config
            config.dockerImage = "${config.dockerRegistry}/${config.name}:${config.version}"

            // set the docker build command
            dockerBuildCommand = dockerBuildCommand ?: config.project.dockerBuildCommand

            // run build command specified container
            container("docker") {
                dockerBuildCommand(config)
            }

            // flag represents that docker image has been created
            config.dockerImageCreated = true
        }
    }
}