#!/bin/bash

# docker build \
# -t node-red \
# -f ../Dockerfile ..

docker build \
-t node-red:slim \
--build-arg BASE_IMAGE_SUFFIX="-slim" \
-f ../Dockerfile ..

# docker build \
# -t node-red:arm \
# --build-arg BASE_NODE_IMAGE="arm32v7/node" \
# -f ../Dockerfile ..

docker build \
-t node-red:arm-slim \
--build-arg BASE_NODE_IMAGE="arm32v7/node" \
--build-arg BASE_IMAGE_SUFFIX="-slim" \
-f ../Dockerfile ..
