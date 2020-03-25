/**
 * Builds the application on the Jenkins pod.
 *
 * Developers can override the the build command by setting the following in the Jenkinsfile:
 *
 *   skipBuild    = false
 *   buildCommand = { code... }
 *
 * @param config       the pipeline config
 * @param buildCommand the provided build command
 */
def call(Map config = [:], Closure buildCommand) {
    // check if we are skipping build stage
    if (config.skipBuild == true) {
        echo "Skipping build stage..."
    } else {
        buildCommand = buildCommand ?: config.project.buildCommand
        // run build command specified container
        container(config.project.container) {
            buildCommand()
        }
    }
}