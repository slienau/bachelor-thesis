#!/bin/bash

DOCKERFILE="Dockerfile"
if [[ "$(uname -m)" == "arm"* ]]; then
    DOCKERFILE="Dockerfile.arm"
fi

docker build \
-t object-detection-server \
-f ../$DOCKERFILE ..
