/**
 * Slack notification step.
 *
 * Developers can skip the stage by setting the following in their Jenkinsfile:
 *
 *   skipSlackNotification = false
 *
 * @param config       the pipeline config
 * @param currentBuild the current build jenkins object
 */
def call(Map config = [:], currentBuild) {
    // check if the slack channel and message are set
    if (!config.slackChannel || config.skipSlackNotification == true) {
        echo "Skipping slack notification stage..."
    } else {
        // slack message color
        def SLACK_COLOR_MAP = ['SUCCESS': 'good', 'FAILURE': 'danger', 'UNSTABLE': 'danger', 'ABORTED': 'danger']
        def color = SLACK_COLOR_MAP[currentBuild.currentResult]
        // slack message content
        def startedBy = currentBuild?.rawBuild?.getCause(Cause.UserIdCause)?.getUserId() ?: ":git: GitHub"
        def lastCommitters = gitHelper.getLastCommitters().join(", ")
        def message = "*${currentBuild.currentResult}:* Job ${JOB_NAME} - Build <${BUILD_URL}|#${BUILD_NUMBER}>\n Started by: ${startedBy}\n Committers: ${lastCommitters}"
        // send slack message
        slackSend (channel: config.slackChannel, color: color, message: message)
    }
}