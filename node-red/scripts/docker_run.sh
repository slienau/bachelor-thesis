#!/bin/bash

source env.sh

DOCKER_IMAGE_TAG="slim"
if [ $IS_ARM == TRUE ]; then
    DOCKER_IMAGE_TAG="arm-slim"
fi

DOCKER_DEVICE_PARAM=""
VIDEO_DEVICE=/dev/video0
if [ -e "$VIDEO_DEVICE" ]; then
    echo "$VIDEO_DEVICE exists"
    DOCKER_DEVICE_PARAM="--device=${VIDEO_DEVICE}:${VIDEO_DEVICE} "
fi

docker run -it -d $DOCKER_DEVICE_PARAM\
--restart=unless-stopped \
--name $DOCKER_CONTAINER_NAME \
--hostname=$HOSTNAME \
-v $DOCKER_VOLUME_NAME:/data \
-e MQTT_SERVER=$MQTT_SERVER \
-e CONNECTED_HARDWARE=$CONNECTED_HARDWARE \
-e PUBLIC_FQDN=$PUBLIC_FQDN \
-e PUBLIC_PORT=$PUBLIC_PORT \
-p $PUBLIC_PORT:1880 \
$DOCKER_IMAGE:$DOCKER_IMAGE_TAG