/**
 * Publishes an artifact to artifactory.
 *
 * Developers can override the artifact publish command by setting the following in the Jenkinsfile:
 *
 *   skipArtifactPublish    = false
 *   artifactPublishCommand = { ctx -> code... }
 *
 * @param config                 the pipeline config
 * @param artifactPublishCommand the provided artifact publish command
 */
def artifactPublish(Map config = [:], Closure artifactPublishCommand) {
    // check if we are skipping artifact publish stage and artifact is created
    if (config.skipArtifactPublish == true) {
        echo "Skipping artifact publish stage..."
    } else {
        // set the artifact publish command
        artifactPublishCommand = artifactPublishCommand ?: config.project.artifactPublishCommand
        // set the git scope so we can write back to git
        gitHelper.runInScope(
            config,
            { ctx ->
                // run publish command in project container
                container(config.project.container) {
                    withCredentials([string(credentialsId: config.artifactoryCredentials, variable: 'JFROG_PASSWORD')]) {
                        env.JFROG_USERNAME = config.artifactoryCredentials
                        artifactPublishCommand(ctx)
                    }
                }
            }
        )
    }
}