#!/usr/bin/env python

# install docker api as
# pip install --user docker
import docker

client = docker.from_env()

iname = 'dcrypps:regression'
image = None
try:
    image = client.images.get(iname)
except docker.errors.ImageNotFound:
    print 'Image', iname, ' not found'

if image is not None:
    print 'Removing image', iname
    client.images.remove(iname)