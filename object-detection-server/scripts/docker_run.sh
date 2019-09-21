#!/bin/bash

source ../../env.sh
docker run --name od-server -it -d --restart=unless-stopped --cpus=$CPU_LIMIT -p 6001:6001 object-detection-server
