/**
 * Runs SonarQube scanning for build.
 *
 *   skipSonarScan = false
 *
 * @param config the pipeline config
 */
def call(Map config = [:]) {
    // check if we are skipping docker push stage (or docker build stage)
    if (config.skipSonarScan == true) {
        echo "Skipping SonarQube scan stage..."
    } else {
        // sonarqube default props
        def sonarDefaultProps = [
            "sonar.scm.provider": "git"
        ]

        // reads properties from sonar-project.properties file
        def sonarFileProps = new Properties()
        if (fileExists("sonar-project.properties")) {
            def content = readFile("sonar-project.properties")
            sonarFileProps.load(new ByteArrayInputStream(content.getBytes()))
        }

        // sets the configProps -> sonarProps argument mappings
        def sonarKeyMappings = [
            "projectKey":                 "sonar.projectKey",
            "projectName":                "sonar.projectName",
            "projectVersion":             "sonar.projectVersion",
            "sources":                    "sonar.sources",
            "sourceEncoding":             "sonar.sourceEncoding",
            "tests":                      "sonar.tests",
            "testExecutionReportPaths":   "sonar.testExecutionReportPaths",
            "genericUnitTestReportPaths": "sonar.genericcoverage.unitTestReportPaths",
            // JVM conversions
            "jacocoXmlReportPaths":       "sonar.coverage.jacoco.xmlReportPaths",
            "junitReportPaths":           "sonar.junit.reportPaths",
            "javaBinaries":               "sonar.java.binaries",
            // Scala conversions
            "scalaVersion":               "sonar.scala.version",
            "scalaCoverageReportPath":    "sonar.scala.scoverage.reportPath",
            "scalaScapegoatReportPath":   "sonar.scala.scapegoat.reportPath",
            // Javascript conversions
            "jstestReportsPath":          "sonar.javascript.jstest.reportsPath",
            "jsLcovReportPaths":          "sonar.javascript.lcov.reportPaths",
            // Typescript conversions
            "tsLintConfigPath":           "sonar.ts.tslint.configPath",
            "tsLcovReportPaths":          "sonar.ts.coverage.lcovReportPath",
        ]

        // convert configProps into sonarProps arguments
        //   i.e. projectVersion -> sonar.projectName
        def sonarConfigProps = (config.sonar ?: [:]).inject([:]) { props, k, v -> props + [ (sonarKeyMappings[k]) : v] }
        
        // apply pipeline sonar configs over sonar-project.properties
        def sonarScannerArgs = (sonarDefaultProps << sonarFileProps << sonarConfigProps).collect { k, v -> "-D$k=$v" }.join(" ")

        // run the sonar-scanner tool
        container("sonar-scanner") {
            withSonarQubeEnv("sonarqube-k8s") {
                sh("sonar-scanner ${sonarScannerArgs}")
            }
        }
    }
}