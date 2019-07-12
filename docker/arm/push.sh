#!/bin/bash

docker tag object-detection-server:arm dsl-mbp:5000/object-detection-server:arm
docker push dsl-mbp:5000/object-detection-server:arm
docker rmi dsl-mbp:5000/object-detection-server:arm