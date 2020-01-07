#!/bin/bash

docker run -it -d \
--restart=unless-stopped \
--name nr-flow-db \
-v nr-flow-db:/data \
-p 2880:1880 \
node-red-flow-database

