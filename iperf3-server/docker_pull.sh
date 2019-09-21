#!/bin/bash

source env.sh

if [ $IS_ARM == TRUE ]; then
    DOCKER_IMAGE=$DOCKER_IMAGE:arm
fi

docker pull $DOCKER_REGISTRY:5000/$DOCKER_IMAGE
docker tag $DOCKER_REGISTRY:5000/$DOCKER_IMAGE $DOCKER_IMAGE
docker rmi $DOCKER_REGISTRY:5000/$DOCKER_IMAGE