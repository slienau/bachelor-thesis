#!/bin/bash

docker build \
-t object-detection-server:arm \
-f Dockerfile ../..
