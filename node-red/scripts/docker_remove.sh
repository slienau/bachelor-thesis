#!/bin/bash

docker stop nodered
docker rm nodered
docker volume rm nodered-data