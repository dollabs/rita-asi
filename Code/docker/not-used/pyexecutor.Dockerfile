# Build a and run docker image for this repository
# 1. make sure docker is installed
# 2. make sure you have a clean copy of this repository
# 3. go to the directory where this file exists (the root of your repo)
# 4. $ docker build -t webgme-py-core-executor -f DockerfilePyCoreExecutor .

FROM nikolaik/python-nodejs:python3.8-nodejs10
MAINTAINER Tamas Kecskes <tamas.kecskes@vanderbilt.edu>

RUN pip3 install webgme-bindings
RUN pip3 install jinja2
RUN pip3 install pymultigen

RUN mkdir /usr/app

WORKDIR /usr/app

# copy app source
COPY code/webgme-dcrypps /usr/app/

# Install node-modules
RUN npm install

RUN cp /usr/app/node_modules/webgme-docker-worker-manager/dockerworker.js /usr/app/dockerworker.js
