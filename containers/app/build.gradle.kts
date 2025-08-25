import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.DockerTagImage
import com.bmuschko.gradle.docker.tasks.image.DockerPushImage

plugins {
    id("com.palantir.git-version") version "4.0.0"
    id("com.bmuschko.docker-remote-api") version "9.4.0"
}

val versionDetails: groovy.lang.Closure<com.palantir.gradle.gitversion.VersionDetails> by extra
val details = versionDetails()
details.lastTag
details.commitDistance
details.gitHash
details.gitHashFull // full 40-character Git commit hash
details.branchName // is null if the repository in detached HEAD mode
details.isCleanTag

val containerImageName = "ghcr.io/lcollins/hello-world-app"
val containerImageTag = "${details.gitHash}"

tasks.register<DockerBuildImage>("dockerTagImage") {
    inputDir.set(file(project.projectDir)) // Use the project root as the Docker context
    images.addAll(
        "$containerImageName:$containerImageTag",
        "$containerImageName:${details.branchName}-latest",
        "$containerImageName:stable")
    pull.set(true) // Ensure the base image is pulled
    noCache.set(false)
}

//tasks.register<DockerTagImage>("dockerTagImage") {
//    imageId.set("$containerImageName:$containerImageTag")
//    repository.set(containerImageName)
//    tag.set("${details.branchName}-latest")
//    dependsOn("buildDockerImage")
//}

tasks.register<DockerPushImage>("dockerPushImage") {
    images.addAll(
        listOf(
            "$containerImageName:$containerImageTag",
            "$containerImageName:${details.branchName}-latest",
            "$containerImageName:stable"
        )
    )
    dependsOn("dockerTagImage")
}

tasks.named("dockerPushImage") {
    dependsOn("dockerTagImage")
}
