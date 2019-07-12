#!/bin/bash

docker pull dsl-mbp:5000/object-detection-server:arm
docker tag dsl-mbp:5000/object-detection-server:arm object-detection-server:arm
docker rmi dsl-mbp:5000/object-detection-server:arm