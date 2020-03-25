/**
 * Checks that Jenkinsfile has set all required fields for the pipeline.
 *
 * @param config the pipeline config
 */
def call(Map config = [:], List requiredValues = []) {
    // find the missing values
    def missingValues = requiredValues.findAll { !config.containsKey(it) }

    // circuit-break pipeline if any missing values are found
    if (missingValues.size() != 0) {
        error "The following Jenkinsfile values are required: ${missingValues}"
    }
}