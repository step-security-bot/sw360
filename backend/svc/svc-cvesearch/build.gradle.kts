/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    id("org.eclipse.sw360.java-conventions")
    war
}

dependencies {
    api(project(":src-cvesearch"))
}

description = "svc-cvesearch"
