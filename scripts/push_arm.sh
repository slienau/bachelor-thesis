#!/bin/bash

source env.sh

docker tag $DOCKER_IMAGE:arm-slim $DOCKER_REGISTRY:5000/$DOCKER_IMAGE:arm-slim
docker push $DOCKER_REGISTRY:5000/$DOCKER_IMAGE:arm-slim
docker rmi $DOCKER_REGISTRY:5000/$DOCKER_IMAGE:arm-slim