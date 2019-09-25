#!/bin/bash

docker run -it -d \
--restart=unless-stopped \
--name iperf3-server \
-p 5201:5201 \
iperf3-server
