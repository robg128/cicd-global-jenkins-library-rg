/**
 * Tests the application on the Jenkins pod.
 *
 * Developers can override the the test command by setting the following in the Jenkinsfile:
 *
 *   skipTests     = false
 *   testCommand   = { code... }
 *
 * @param config      the pipeline config
 * @param testCommand the provided test command
 */
def call(Map config = [:], Closure testCommand) {
    // check if we are skipping build stage
    if (config.skipTests == true || config.skipUnitTests == true) {
        echo "Skipping tests stage..."
    } else {
        testCommand = testCommand ?: config.project.testCommand
        // run build command specified container
        container(config.project.container) {
            testCommand()
        }
    }
}