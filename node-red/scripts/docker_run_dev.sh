#!/bin/bash

source ../../env.sh

# mount video device to docker container if it exists
DOCKER_DEVICE_PARAM=""
VIDEO_DEVICE=/dev/video0
if [ -e "$VIDEO_DEVICE" ]; then
    echo "$VIDEO_DEVICE exists"
    DOCKER_DEVICE_PARAM="--device=${VIDEO_DEVICE}:${VIDEO_DEVICE} "
fi

# set CPU limit for non-arm architecture (not supported by arm)
if [[ "$(uname -m)" == "arm"* ]]; then
docker run -it -d $DOCKER_DEVICE_PARAM\
    --restart=unless-stopped \
    --name nodered \
    --hostname=$HOSTNAME \
    -v nodered-data:/data \
    -e MQTT_SERVER=$MQTT_SERVER \
    -e CONNECTED_HARDWARE=$CONNECTED_HARDWARE \
    -e PUBLIC_FQDN=$PUBLIC_FQDN \
    -e PUBLIC_PORT=$PUBLIC_PORT \
    -p $PUBLIC_PORT:1880 \
    -v "$(pwd)"/../..:/root/bachelor-thesis \
    node-red
else
    docker run -it -d $DOCKER_DEVICE_PARAM\
    --restart=unless-stopped \
    --name nodered \
    --hostname=$HOSTNAME \
    -v nodered-data:/data \
    -e MQTT_SERVER=$MQTT_SERVER \
    -e CONNECTED_HARDWARE=$CONNECTED_HARDWARE \
    -e PUBLIC_FQDN=$PUBLIC_FQDN \
    -e PUBLIC_PORT=$PUBLIC_PORT \
    -p $PUBLIC_PORT:1880 \
    -v "$(pwd)"/../..:/root/bachelor-thesis \
    --cpus=$CPU_LIMIT \
    node-red
fi
