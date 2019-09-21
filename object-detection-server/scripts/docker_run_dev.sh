#!/bin/bash

docker run --name od-server -it --rm -p 6001:6001 -v $PWD/../src:/usr/src/object_detection_app object-detection-server