FROM ubuntu:20.04

## Build image as
##   docker build -t asist_base_map .

## Run as
##  docker run --rm -u $(id -u ${USER}):$(id -g ${USER})  -e "asist_testbed=/testbed" -v $PWD:/work -v$asist_testbed:/testbed asis_base_map  /work/make_asist_base_map.sh Falcon

RUN apt-get update -y && \
    apt-get install python3-pip python3-dev openjdk-8-jdk-headless -y

COPY ./mcworldlib /mcworldlib
RUN pip3 install /mcworldlib pandas ipython pillow

ENV PYTHONPATH=/mcworldlib
WORKDIR /work
CMD /bin/bash
