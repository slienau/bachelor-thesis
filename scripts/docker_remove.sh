#!/bin/bash

source env.sh

docker stop $DOCKER_CONTAINER_NAME
docker rm $DOCKER_CONTAINER_NAME
docker volume rm $DOCKER_VOLUME_NAME