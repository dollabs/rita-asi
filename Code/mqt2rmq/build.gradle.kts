/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java project to get you started.
 * For more details take a look at the Java Quickstart chapter in the Gradle
 * User Manual available at https://docs.gradle.org/6.1/userguide/tutorial_java_projects.html
 */

plugins {
    // Apply the java plugin to add support for Java
    java

    // Apply the application plugin to add support for building a CLI application.
    application

    // for uberjar
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

repositories {
    // Use jcenter for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.
    jcenter()
}

dependencies {
    // This dependency is used by the application.
//    implementation("com.google.guava:guava:28.1-jre")
    implementation("com.rabbitmq:amqp-client:5.8.0")
    implementation("info.picocli:picocli:4.1.4")
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.2")
    implementation("org.jetbrains:annotations:19.0.0")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("org.slf4j:slf4j-simple:1.6.1")
    // Use JUnit test framework
    testImplementation("junit:junit:4.12")
}

application {
    // Define the main class for the application.
    mainClassName = "mqt2rmq.App"
}