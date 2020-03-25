/**
 * General step in the pipeline.
 *
 * @param config      the pipeline config
 * @param stepCommand the provided step command
 */
def call(Map config = [:], Closure stepCommand) {
    // check if we are skipping the stage
    if (!stepCommand) {
        echo "Skipping stage as no command specified."
    } else {
        // run step command specified container
        stepCommand()
    }
}