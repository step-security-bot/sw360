/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    id("org.eclipse.sw360.java-conventions")
}

description = "build-configuration"

val testsJar by tasks.registering(Jar::class) {
    archiveClassifier.set("tests")
    from(sourceSets["test"].output)
}

(publishing.publications["maven"] as MavenPublication).artifact(testsJar)
