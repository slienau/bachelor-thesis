#!/bin/bash

if [ "$1" == "dev" ]
then
    docker run --name od-server -it --rm -p 6001:6001 -v $PWD/../src:/usr/src/object_detection_app object-detection-server
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
    docker run --name od-server -it --rm -p 6001:6001 object-detection-server
fi
