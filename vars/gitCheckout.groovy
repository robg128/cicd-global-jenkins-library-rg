/**
 * Packages a helm chart locally.
 *
 * @param config                the pipeline config
 * @param repo                  the name of the repo to clone
 * @param additionalGitCommands the command to run once the repo has been cloned
 */
def call(Map config = [:], String repo = "", Closure additionalGitCommands = {}) {
    if (repo != "") {
        // run git checkout in 'jnlp' agent container
        container("jnlp") {
            // clone the repo
            git branch: "master", credentialsId: config.githubCredentials, url: "https://github.com/monster-next/${repo}.git"
            // run the additional git commands
            additionalGitCommands()
        }
    } else {
        echo "Repository not set, skipping git checkout step"
    }
}