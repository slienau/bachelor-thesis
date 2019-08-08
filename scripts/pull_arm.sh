#!/bin/bash

source env.sh

docker pull $DOCKER_REGISTRY:5000/$DOCKER_IMAGE:arm-slim
docker tag $DOCKER_REGISTRY:5000/$DOCKER_IMAGE:arm-slim $DOCKER_IMAGE:arm-slim
docker rmi $DOCKER_REGISTRY:5000/$DOCKER_IMAGE:arm-slim