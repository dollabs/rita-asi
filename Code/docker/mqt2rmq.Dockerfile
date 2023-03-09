FROM ubuntu:20.04

ENV APPDIR /Applications
RUN mkdir $APPDIR

RUN apt-get update && apt-get install openjdk-8-jdk-headless netcat -y

RUN mkdir $APPDIR/mqt2rmq
WORKDIR $APPDIR/mqt2rmq

# Setup dependencies
COPY mqt2rmq/build.gradle.kts .
COPY mqt2rmq/gradle $APPDIR/mqt2rmq/gradle
COPY mqt2rmq/gradlew .
RUN ./gradlew

# Build code
COPY mqt2rmq $APPDIR/mqt2rmq
RUN ./gradlew build

COPY docker/mqt2rmq-entry-point.sh $APPDIR/mqt2rmq
ENV PATH $APPDIR/mqt2rmq:$PATH
RUN mkdir $APPDIR/logs

# Copy the bin files and make sure they're executuable
RUN mkdir $APPDIR/bin
COPY bin $APPDIR/bin

WORKDIR $APPDIR/bin
RUN ls | grep .sh | xargs chmod +x

ENTRYPOINT ["mqt2rmq-entry-point.sh"]
CMD bash
