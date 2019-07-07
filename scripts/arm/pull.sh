#!/bin/bash

docker pull dsl-mbp:5000/node-red:arm-slim
docker tag dsl-mbp:5000/node-red:arm-slim node-red:arm-slim
docker rmi dsl-mbp:5000/node-red:arm-slim