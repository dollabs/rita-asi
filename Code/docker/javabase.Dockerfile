# This image comes with:
# - openjdk-8
# - curl
# - git
# - netcat

# To build,
#   docker build -t registry.gitlab.com/dollabsp/docker/javabase -f javabase.Dockerfile .
# To push to Dollabs public docker repo,
#   docker push registry.gitlab.com/dollabsp/docker/javabase

FROM ubuntu:16.04

ENV APPDIR /Applications
RUN mkdir $APPDIR

RUN apt-get update && apt-get install openjdk-8-jdk-headless curl git-core netcat -y

CMD java -version
