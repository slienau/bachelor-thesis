#!/bin/bash

source env.sh

docker build \
--build-arg BASE_IMAGE="alpine" \
-t $DOCKER_IMAGE \
-f Dockerfile .

docker build \
--build-arg BASE_IMAGE="arm32v7/alpine" \
-t $DOCKER_IMAGE:arm \
-f Dockerfile .