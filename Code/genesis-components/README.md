# Genesis and START Within RITA

This provides a simple Java framework for the Genesis and START components of RITA.  It is intended that the build framework will used to build Genesis and to provide RabbitMQ-based messaging with the other RITA components.

## Genesis in the RITA Docker-based Environment
Genesis (in the form of the stub `App.java`) is built and executed as part of the RITA Docker-based Environment.  Please see the *RITA Implementation* manual in the *Documentation* directory for complete details.

To view the output of the Docker-based Genesis component: `docker-compose logs genesis`

## Build
 * To build, `./gradlew clean && ./gradlew build`. This will create jar and uberjar jar files in `./build/lib.`

```
ll -lsath build/libs/
total 5808
   0 drwxr-xr-x  11 prakash  staff   352B Jan 16 21:30 ..
5800 -rw-r--r--   1 prakash  staff   2.8M Jan 16 21:30 genesis-components-all.jar
   0 drwxr-xr-x   4 prakash  staff   128B Jan 16 21:30 .
   8 -rw-r--r--   1 prakash  staff   1.0K Jan 16 21:30 genesis-components.jar
```

## Run uberjar
 * `build/lib/genesis-components-all.jar` is the uberJar that could be invoked as `java -jar build/libs/genesis-components-all.jar`
 * For now it simply connects to the RabbitMQ server, and subscribes to the `"startup-rita"` topic.  When a `"startup-rita"` message is received, a simple message is printed.


```
java -jar build/libs/genesis-components-all.jar

Connecting to RMQ:
Host: rabbitmq
Port: 5672
Exchange: rita
Main waiting for messages
Control-C to quit

[Later, when startup-rita is published...]
 [x] Received 'startup-rita':'{"mission-id":"mission53","timestamp":1579820738495,"routing-key":"startup-rita","app-id":"RITAControlPanel"}'

```

## Gradle build tool
This is only needed if you wish to work regularly with gradle on multiple projects and is here for reference only.

 * Download `curl -L https://services.gradle.org/distributions/gradle-6.1-bin.zip -o gradle-6.1-bin.zip`
 * Install to your preferred location.
   * Ex: `unzip gradle-6.1-bin.zip && mv -i ~/Applications/gradle-6.1`
   * Add `~/Applications/gradle-6.1/bin` to your `PATH`
 * Verify gradle works as in `gradle -v`

```
gradle -v

Welcome to Gradle 6.1!

Here are the highlights of this release:
 - Dependency cache is relocatable
 - Configurable compilation order between Groovy, Java & Scala
 - New sample projects in Gradle's documentation

For more details see https://docs.gradle.org/6.1/release-notes.html


------------------------------------------------------------
Gradle 6.1
------------------------------------------------------------

Build time:   2020-01-15 23:56:46 UTC
Revision:     539d277fdba571ebcc9617a34329c83d7d2b259e

Kotlin:       1.3.61
Groovy:       2.5.8
Ant:          Apache Ant(TM) version 1.10.7 compiled on September 1 2019
JVM:          1.8.0_192 (Oracle Corporation 25.192-b12)
OS:           Mac OS X 10.14.6 x86_64

```
