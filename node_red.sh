#!/bin/bash
CONTAINER_NAME=nodered
DOCKER_IMAGE=node-red:slim
MQTT_SERVER=192.168.7.13

if [ "$1" == "run" ]
then
    docker run -it -d \
    --restart=unless-stopped \
    --name $CONTAINER_NAME \
    --hostname=$HOSTNAME \
    -e MQTT_SERVER=$MQTT_SERVER \
    -p 1880:1880 \
    $DOCKER_IMAGE
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
