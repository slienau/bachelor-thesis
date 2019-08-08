#!/bin/bash
CONTAINER_NAME=nodered
DOCKER_IMAGE=node-red
MQTT_SERVER=192.168.7.13

IS_ARM=FALSE
if [[ "$(uname -m)" == "arm"* ]]; then
    IS_ARM=TRUE
fi

DOCKER_IMAGE_TAG="slim"
if [ $IS_ARM == TRUE ]; then
    DOCKER_IMAGE_TAG="arm-slim"
fi

if [ "$1" == "run" ]
then
    DOCKER_DEVICE_PARAM=""
    VIDEO_DEVICE=/dev/video0
    if [ -e "$VIDEO_DEVICE" ]; then
        echo "$VIDEO_DEVICE exists"
        DOCKER_DEVICE_PARAM="--device=${VIDEO_DEVICE}:${VIDEO_DEVICE} "
    fi

    docker run -it -d $DOCKER_DEVICE_PARAM\
    --restart=unless-stopped \
    --name $CONTAINER_NAME \
    --hostname=$HOSTNAME \
    -e MQTT_SERVER=$MQTT_SERVER \
    -p 1880:1880 \
    $DOCKER_IMAGE:$DOCKER_IMAGE_TAG
elif [ "$1" == "start" ]
then
    docker start $CONTAINER_NAME
elif [ "$1" == "restart" ]
then
    docker restart $CONTAINER_NAME
elif [ "$1" == "stop" ]
then
    docker stop $CONTAINER_NAME
elif [ "$1" == "remove" ]
then
    docker stop $CONTAINER_NAME
    docker rm $CONTAINER_NAME
elif [ "$1" == "attach" ]
then
    docker attach $CONTAINER_NAME
elif [ "$1" == "bash" ]
then
    docker exec -it $CONTAINER_NAME bash
else
    echo 'Please provide valid argument'
    exit 1
fi
