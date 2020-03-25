/**
 * Tags the git repository for the release of a new artifact (docker image, package).
 *
 * @param config the pipeline config
 */
def call(Map config = [:]) {
    // check if on master branch and tag is defined
    if (env.BRANCH_NAME == "master" && !!config.version) {
        // tag repo with docker tag
        gitHelper.runInScope(
            config,
            { ctx ->
                sh("git tag v${ctx.version}")
                sh("GIT_ASKPASS=true git push origin --tags")
            }
        )
    } else {
        echo "No tag found, skipping tag repository step"
    }
}