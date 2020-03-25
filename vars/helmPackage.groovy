/**
 * Packages a helm chart locally.
 *
 * Developers can override the helm package command by setting the following in the Jenkinsfile:
 *
 *   skipHelmPackage    = false
 *   helmPackageCommand = { code... }
 *
 * @param config             the pipeline config
 * @param helmPackageCommand the provided helm package command
 */
def call(Map config = [:], Closure helmPackageCommand) {
    // check if we are skipping helm package stage
    if (config.skipHelmPackage == true) {
        echo "Skipping helm package stage..."
    } else {

        dir("helm-repo") {
            gitCheckout (config, config.helmChartRepo)
        
            // if master branch, pull values from prod values
            if ( env.BRANCH_NAME == "master" ) {
                config.helmEnvironment = "prod"
            // else if PR with master target
            } else if ( env.CHANGE_ID && env.CHANGE_TARGET == "master" ) {
                config.helmEnvironment = "stag"
            }

            // set the helm package command
            helmPackageCommand = helmPackageCommand ?: { Map ctx ->
                sh("wget https://github.com/mikefarah/yq/releases/download/3.2.1/yq_linux_amd64 -O /usr/bin/yq && chmod 755 /usr/bin/yq")
                sh("apk update && apk add bash")
                sh("helm plugin install plugins/subchart")
                // Use Subchart plugin
                sh("helm subchart package ${config.name} --version=${config.version} --app-version=${config.version} --env ${config.helmEnvironment}")
                sh(" mv ${config.name}-${config.version}.tgz ../${config.name}-${config.version}.tgz ")
            }

            // run package command in 'helm' container
            container('helm') {
                helmPackageCommand(config)
            }
        }

        // set helm artifact name for downstream 'helmPublish' stage
        config.helmArtifact = "${config.name}-${config.version}.tgz"
    }
}
