#!/usr/bin/env bash



## Script for docker related testing.
program=$(basename $0)
pdir=$(dirname $0)
cd $pdir
cd ../../
# Top level of the git repo.
PROJ_HOME=$(pwd -P)
export COMPOSE_PROJECT_NAME=$(whoami)
echo "---------------------"
echo "Running under ${COMPOSE_PROJECT_NAME}"
echo "---------------------"
echo "0) Previous list of images"
echo "---------------------"
docker images

echo "---------------------"
echo "1) Cleaning up"
echo "---------------------"
# Ensure all containers are stopped
docker-compose stop
# Clean up all stopped containers, unused networks etc.
docker system prune -f
echo "---------------------"
echo "2) Building compose images"
echo "---------------------"
# Override the default Planviz port since Jenkins is running on 8080
export PLANVIZ_PORT=8087
docker-compose -f docker-compose.yml build --no-cache
if [ $? -ne 0 ]; then
 echo "Building images failed"
 exit 1
fi

# Add tests here

echo "---------------------"
echo "5)Done -------"
echo "---------------------"

## Notes
### javabase, mongo and rabbit containers will always rebuild when --no-cache option is used. Since rebuilding
### them does not take too long, we are ok for now.
