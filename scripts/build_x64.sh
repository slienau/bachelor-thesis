#!/bin/bash

source env.sh

docker build \
-t $DOCKER_IMAGE:slim \
--build-arg BASE_IMAGE_SUFFIX="-slim" \
-f ../Dockerfile ..
