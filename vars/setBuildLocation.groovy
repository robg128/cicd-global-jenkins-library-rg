/**
 * Sets location variables for the build.
 *
 * @param config the pipeline config
 */
def call(Map config = [:]) {
    def defaults = [
        // AP -> Sydney
        "AP": [
            // credentials
            githubCredentials: "github-user",
            artifactoryCredentials: "artifactory-cicd-pipeline-user",
            // registries
            dockerRegistry: "monsternextsyd-docker.jfrog.io",
            // helm info
            helmRepo: "helm-local",
            helmChartRepo: "torana-spin-helm-charts",
        ],
        // US -> Weston
        "US": [
            // credentials
            githubCredentials: "Cloudbees-Github-User",
            artifactoryCredentials: "artifactory-cicd-pipeline-user",
            // registries
            dockerRegistry: "monsternext-docker.jfrog.io",
            // helm info
            helmRepo: "helm-local",
            helmChartRepo: "torana-spin-helm-charts",
        ],
        // EU -> Czech
        "EU": [
            // credentials
            githubCredentials: "Cloudbees-Github-User",
            artifactoryCredentials: "artifactory-cicd-pipeline-user",
            // registries
            dockerRegistry: "monsternext-docker.jfrog.io",
            // helm info
            helmRepo: "helm-local",
            helmChartRepo: "torana-spin-helm-charts"
        ]
    ]

    // get the BUILD_LOCATION env var set in Jenkins.
    // set via Jenkins > configuration > Global properties > Environment variables
    def buildLocation = "${BUILD_LOCATION}"
    
    // checks if BUILD_LOCATION is valid
    if (!defaults[buildLocation]) {
        error "Invalid 'BUILD_LOCATION' (value='${buildLocation}'). Known locations are: ${defaults.keySet()}. Please contact your Jenkins administrator to define the 'BUILD_LOCATION' global var."
    }

    // set the location defaults on the pipeline if not already defined
    defaults[buildLocation].each { key, defaultValue ->
        if (!config.containsKey(key)) config[key] = defaultValue
    }
}