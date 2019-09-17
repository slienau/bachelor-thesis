#!/bin/bash

ARCHITECTURE="$(uname -m)"
IS_ARM=FALSE
# IS_ARM=TRUE # for testing on non-arm platforms

if [[ "$(uname -m)" == "arm"* ]]; then
    IS_ARM=TRUE
fi

DOCKER_IMAGE_TAG="latest"
if [ $IS_ARM == TRUE ]; then
    DOCKER_IMAGE_TAG="arm"
fi

if [ "$1" == "dev" ]
then
    docker run --name od-server -it --rm -p 6001:6001 -v $PWD/../src:/usr/src/object_detection_app object-detection-server:$DOCKER_IMAGE_TAG
elif [ "$1" == "attach" ]
then
    docker attach od-server
elif [ "$1" == "bash" ]
then
    docker exec -it od-server bash
elif [ "$1" == "stop" ]
then
    docker stop od-server
else
    docker run --name od-server -it --restart=unless-stopped -p 6001:6001 object-detection-server:$DOCKER_IMAGE_TAG
fi
