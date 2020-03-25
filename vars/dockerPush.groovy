/**
 * Pushes a docker image to artifactory.
 *
 * Developers can override the push command by setting the following in the Jenkinsfile:
 *
 *   skipDockerPush    = false
 *   dockerPushCommand = { code... }
 *
 * @param config            the pipeline config
 * @param dockerPushCommand the provided docker push command
 */
def call(Map config = [:], Closure dockerPushCommand) {
    // check if we are skipping docker push stage (or docker build stage)
    if (config.skipDockerPush == true || !config.dockerImageCreated) {
        echo "Skipping docker push stage..."
    } else {
        // set the docker build command
        dockerPushCommand = dockerPushCommand ?: { sh("docker push ${config.dockerImage}") }

        // run build command specified container
        container("docker") {
            withCredentials([string(credentialsId: config.artifactoryCredentials, variable: 'artifactoryCredentials')]) {
                sh("docker login '${config.dockerRegistry}' -u ${config.artifactoryCredentials} -p '${artifactoryCredentials}'")
                dockerPushCommand()
            }
        }

        // check if we need to tag git repo with docker tag
        if (!config.skipDockerRepoTag) {
            tagRepository(config)
        }
    }
}