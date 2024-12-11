import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.DockerTagImage
import com.bmuschko.gradle.docker.tasks.image.DockerPushImage

plugins {
    //id("java")
    id("com.palantir.git-version") version "3.1.0"
    id("com.bmuschko.docker-remote-api") version "9.3.1"
}

val containerImageName = "ghcr.io/lcollins/hello-world-app"
val containerImageTag = "1.0.0"

tasks.register<DockerBuildImage>("buildDockerImage") {
    inputDir.set(file(project.projectDir)) // Use the project root as the Docker context
    images.add("$containerImageName:$containerImageTag") // Correctly specify the image tags
    pull.set(true) // Ensure the base image is pulled
    noCache.set(false)
}

tasks.register<DockerTagImage>("dockerTagImage") {
    imageId.set("$containerImageName:$containerImageTag")
    repository.set(containerImageName)
    tag.set("latest")
    dependsOn("buildDockerImage")
}

tasks.register<DockerPushImage>("dockerPushImage") {
    images.addAll(
        listOf(
            "$containerImageName:$containerImageTag",
            "$containerImageName:latest"
        )
    )
    dependsOn("dockerTagImage")
}

tasks.named("dockerPushImage") {
    dependsOn("dockerTagImage")
}

//tasks.named("build") {
//    dependsOn("dockerPushImage")
//}
