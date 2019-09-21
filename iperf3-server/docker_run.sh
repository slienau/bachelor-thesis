#!/bin/bash

source env.sh

if [ $IS_ARM == TRUE ]; then
    DOCKER_IMAGE=$DOCKER_IMAGE:arm
fi

docker run -it -d \
--restart=unless-stopped \
--name $DOCKER_CONTAINER_NAME \
-p 5201:5201 \
$DOCKER_IMAGE