#  cicd-global-jenkins-library

The **CICD Global Jenkins Library** aims to provide a paved road Jenkins CI integration for Monster.NEXT web applications. The benefits of using a paved-road approach vs declarative are that Jenkinsfiles are more concise, require no/minimal changes when adding/removing pipeline stages, abstracts complexity out into the pipeline DSL and also enforces required validations stages be run before application deployment.

## Docker Pipeline

This pipeline should be used by applications that produce a docker artifact.

The structure of the pipeline is as follows:

1. **Build Validation:** verifies Jenkinsfile is correct and populates default build instructions for known project types (i.e. `NodeJS`, `Gradle`, `Maven`)

2. **Build:** builds the application

3. **Unit Test:** unit tests the application

4. **SonarQube Analysis:** sends coverage metrics to SonarQube

5. **Docker Build:** builds the application's docker artifact

6. **Docker Push:** pushes the docker artifact to a docker registry

### Jenkinsfile

```groovy
@Library("cicd-global-library") _

dockerPipeline {
    // the application name
    name = "<application-name>"

    // the docker tag semver (X.X.X)
    major_version = "0"
    minor_version = "0"

    /**************************************************/
    /*************** OPTIONAL OVERRIDES ***************/
    /**************************************************/

    // skips application build stage
    skipBuild = false

    // skips application unit test stage
    skipTests = false

    // skips sonarqube scanning (will be set to false later on)
    skipSonarScan = true

    // skips docker build stage
    skipDockerBuild = false

    // restrict branches where docker artifacts are built
    dockerBranchRegex = ".*"

    // skips docker push stage
    skipDockerPush = false

    // skips git repo tagging on docker push
    skipDockerRepoTag = false

    // channel where slack messages are posted (skipped if not set)
    slackChannel = ""

    // skips slack notification on build completion
    skipSlackNotification = false

    /**
     * Sets the build command for the build.
     *
     * Defaults:
     *
     *  NodeJS -> sh("npm run build")
     *  Gradle -> sh("./gradlew build")
     *  Maven  -> sh("mvn build")
     */
    buildCommand = {
        sh("<build command>")
    }

    /**
     * Sets the test command for the build.
     *
     * Defaults:
     *
     *  NodeJS -> sh("npm run test")
     *  Gradle -> sh("./gradlew test")
     *  Maven  -> sh("mvn test")
     */
    testCommand = {
        sh("<test command>")
    }
}
```

## Artifact Pipeline

This pipeline should be used by common libraries that are published to artifactory.

The structure of the pipeline is as follows:

1. **Build Validation:** verifies Jenkinsfile is correct and populates default build instructions for known project types (i.e. `NodeJS`, `Gradle`, `Maven`)

2. **Build:** builds the application

3. **Unit Test:** unit tests the application

4. **SonarQube Analysis:** sends coverage metrics to SonarQube

5. **Publish Artifact:** performs a release of the artifact to Artifactory

### Demo Artifact Projects

We have created the following demo projects to showcase off onboarding onto the `artifactPipeline`:

 - [NPM Library](https://github.com/monster-next/demo-torana-node-library) using plugins: [ [release-it](https://github.com/release-it/release-it) ]
 - [Gradle Library](https://github.com/monster-next/demo-torana-gradle-library) using plugins: [ [gradle-release](https://github.com/researchgate/gradle-release) + [maven-publish](https://docs.gradle.org/current/userguide/publishing_maven.html) ]
 - [Maven Library](https://github.com/monster-next/demo-torana-maven-library) using plugins: [ [maven-release](http://maven.apache.org/maven-release/maven-release-plugin/usage.html) ]

### Jenkinsfile

```groovy
@Library("cicd-global-library") _

artifactPipeline {
    // the application name
    name = "<application-name>"

    /**************************************************/
    /*************** OPTIONAL OVERRIDES ***************/
    /**************************************************/

    // skips application build stage
    skipBuild = false

    // skips application unit test stage
    skipTests = false

    // skips sonarqube scanning (will be set to false later on)
    skipSonarScan = true

    // skips artifact publishing
    skipArtifactPublish = false

    // channel where slack messages are posted (skipped if not set)
    slackChannel = ""

    // skips slack notification on build completion
    skipSlackNotification = false

    /**
     * Sets the build command for the build.
     *
     * Defaults:
     *
     *  NodeJS -> sh("npm run build")
     *  Gradle -> sh("./gradlew build")
     *  Maven  -> sh("mvn build")
     */
    buildCommand = {
        sh("<build command>")
    }

    /**
     * Sets the test command for the build.
     *
     * Defaults:
     *
     *  NodeJS -> sh("npm run test")
     *  Gradle -> sh("./gradlew test")
     *  Maven  -> sh("mvn test")
     */
    testCommand = {
        sh("<test command>")
    }

    /**
     * Sets the artifact publish command for the build.
     *
     * Defaults:
     *
     *  NodeJS -> { ctx -> ctx.branchName == "master" ? sh("npm run release") : echo("Cannot publish to NPM from feature branch!") }
     *  Gradle -> { ctx -> ctx.branchName == "master" ? sh("./gradlew release") : sh("./gradlew publish") }
     *  Maven  -> { ctx -> ctx.branchName == "master" ? sh("mvn release:prepare release:perform") : sh("mvn deploy") }
     */
    artifactPublishCommand = { ctx ->
        sh("<publish command>")
    }
}
```

## Test Pipeline

This pipeline should be used for integration and performance tests.

The structure of the pipeline is as follows:

1. **Build Validation:** verifies Jenkinsfile is correct and populates default build instructions for known project types (i.e. `NodeJS`, `Gradle`, `Maven`)

2. **Build:** builds the testing application

3. **Unit Test:** runs the tests

4. **Publish Test Reports:** if specified, used to publish HTML test reports in Jenkins

### Jenkinsfile

```groovy
@Library("cicd-global-library") _

testPipeline {
    /**************************************************/
    /*************** OPTIONAL OVERRIDES ***************/
    /**************************************************/

    // skips test application build stage
    skipBuild = false

    // skips test application test stage
    skipTests = false

    // skips slack notification on build completion
    skipSlackNotification = false

    // channel where slack messages are posted (skipped if not set)
    slackChannel = ""

    /**
     * Sets the build command for the build.
     *
     * Defaults:
     *
     *  NodeJS -> sh("npm run build")
     *  Gradle -> sh("./gradlew build")
     *  Maven  -> sh("mvn build")
     */
    buildCommand = {
        sh("<build command>")
    }

    /**
     * Sets the test command for the build.
     *
     * Defaults:
     *
     *  NodeJS -> sh("npm run test")
     *  Gradle -> sh("./gradlew test")
     *  Maven  -> sh("mvn test")
     */
    testCommand = {
        sh("<test command>")
    }

    /**
     * Publishes generated test reports.
     */
    publishTestReportCommand = {}
}
```