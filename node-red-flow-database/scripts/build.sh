#!/bin/bash

# set base image either to "arm32v7/node" (for raspberry pi) or official "node" image (for non-arm architectures)
if [[ "$(uname -m)" == "arm"* ]];
then
    BASE_NODE_IMAGE="arm32v7/node"
else
    BASE_NODE_IMAGE="node"
fi

docker build \
-t node-red-flow-database \
--build-arg BASE_NODE_IMAGE=$BASE_NODE_IMAGE \
-f ../Dockerfile ..
