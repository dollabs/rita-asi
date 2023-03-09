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
    implementation("com.google.guava:guava:28.1-jre")
    implementation("com.rabbitmq:amqp-client:5.8.0")
    implementation("info.picocli:picocli:4.1.4")
    implementation("org.slf4j:slf4j-simple:1.6.1")
    implementation("com.google.code.gson:gson:2.2.2")
    implementation("com.googlecode.json-simple:json-simple:1.1.1")
//    implementation("commons-cli:commons-cli:1.4")
    // Use JUnit test framework
    testImplementation("junit:junit:4.12")
}

application {
    // Define the main class for the application.
    mainClassName = "com.doll.rmq.App"
    // mainClassName = "subsystems.rita.App"
}

// No luck with following code but
//tasks.register<Jar>("uberJar")
//  {
//      archiveClassifier.set("uber")
//      from (sourceSets.main.get().output)
//      dependsOn (configurations.runtimeClasspath)
//      from ({
//          configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) } })
//  }