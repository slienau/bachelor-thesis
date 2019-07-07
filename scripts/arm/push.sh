#!/bin/bash

docker tag node-red:arm-slim dsl-mbp:5000/node-red:arm-slim
docker push dsl-mbp:5000/node-red:arm-slim
docker rmi dsl-mbp:5000/node-red:arm-slim