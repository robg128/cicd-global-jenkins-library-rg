/**
 * Get the users who trigger the build and/or get the list of users from the last changeset.
 */
def getLastCommitters() {
    // check if previous commit is current commit
    def COMMIT_RANGE = (env.GIT_PREVIOUS_COMMIT == env.GIT_COMMIT || env.GIT_PREVIOUS_COMMIT == null) ? "-1" : "${GIT_PREVIOUS_COMMIT}..${GIT_COMMIT}"
    // get the list of users' emails from previous commit til now
    def gitUserLog = sh(returnStdout: true, script:"git log ${COMMIT_RANGE} --pretty=format:%ae")
    // split output by newline ('\n') and remove duplicates
    return gitUserLog.split("\n").toUnique()
}

/**
 * Runs git commands to read/write back to git.
 *
 * @param config     the pipeline config
 * @param gitCommand the git command
 */
def runInScope(Map config, Closure gitCommand) {
    try {
        withCredentials([usernamePassword(credentialsId: config.githubCredentials, usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD')]) {
            sh("git config credential.username $GIT_USERNAME")
            sh("git config credential.helper '!f() { echo password=\$GIT_PASSWORD; }; f'")
            sh("git config user.email 'monsternextci@noreply.com'")
            sh("git config user.name 'MonsterNextCI'")
            gitCommand(config)
        }
    } finally {
        sh("git config --unset credential.username")
        sh("git config --unset credential.helper")
        sh("git config --unset user.email")
        sh("git config --unset user.name")
    }
}