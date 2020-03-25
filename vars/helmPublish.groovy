/**
 * Publishes a helm chart to artifactory.
 *
 * Developers can override the helm publish command by setting the following in the Jenkinsfile:
 *
 *   skipHelmPublish    = false
 *   helmPublishCommand = { ctx -> code... }
 *
 * @param config             the pipeline config
 * @param helmPublishCommand the provided helm publish command
 */
def call(Map config = [:], Closure helmPublishCommand) {
    // check if we are skipping helm publish stage and artifact is created
    if (config.skipHelmPublish == true || !config.helmArtifact) {
        echo "Skipping helm publish stage..."
    } else {
        // set the helm publish command
        helmPublishCommand = helmPublishCommand ?: { Map ctx ->
            def server = Artifactory.server "helm-registry"
            def uploadSpec = """{
                "files": [{
                    "pattern": "${ctx.helmArtifact}",
                    "target": "${ctx.helmRepo}/${ctx.name}/${ctx.helmArtifact}"
                }]
            }"""
            server.upload spec: uploadSpec
        }
        // run publish command in 'helm' container
        container('helm') {
            helmPublishCommand(config)
        }
    }
}