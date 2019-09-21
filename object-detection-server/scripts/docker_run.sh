#!/bin/bash

# set CPU limit for non-arm architecture (not supported by arm)
if [[ "$(uname -m)" == "arm"* ]]; then
    docker run --name od-server -it -d --restart=unless-stopped -p 6001:6001 object-detection-server
else
    source ../../env.sh
    docker run --name od-server -it -d --restart=unless-stopped --cpus=$CPU_LIMIT -p 6001:6001 object-detection-server
fi

