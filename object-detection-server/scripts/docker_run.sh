#!/bin/bash

docker run --name od-server -it -d --restart=unless-stopped -p 6001:6001 object-detection-server
