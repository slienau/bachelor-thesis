#!/bin/bash

if [[ "$(uname -m)" == "arm"* ]]; then
    BASE_IMAGE="arm32v7/alpine"
else
    BASE_IMAGE="alpine"
fi

docker build \
--build-arg BASE_IMAGE=$BASE_IMAGE \
-t iperf3-server \
-f Dockerfile .
