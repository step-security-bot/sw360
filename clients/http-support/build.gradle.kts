/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    id("org.eclipse.sw360.java-conventions")
}

dependencies {
    api("com.squareup.okhttp3:okhttp:4.10.0")
    api("com.fasterxml.jackson.core:jackson-core:2.14.2")
    api("com.fasterxml.jackson.core:jackson-databind:2.14.2")
    api("commons-io:commons-io:2.11.0")
    api("org.slf4j:slf4j-api:1.7.30")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:2.28.2")
    testImplementation("org.assertj:assertj-core:3.11.1")
    testImplementation("com.github.tomakehurst:wiremock-jre8:2.26.0")
    testImplementation("nl.jqno.equalsverifier:equalsverifier:3.1.12")
    testImplementation("org.slf4j:slf4j-simple:1.7.30")
    testImplementation("javax.servlet:javax.servlet-api:4.0.1")
}

description = "http-support"
