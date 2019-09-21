export DOCKER_CONTAINER_NAME=nodered
export DOCKER_IMAGE=node-red
export DOCKER_VOLUME_NAME=nodered-data
export PUBLIC_FQDN=dsl89.ddns.net
export PUBLIC_PORT=1880
export MQTT_SERVER=dslbabroker.westeurope.azurecontainer.io

CONNECTED_HARDWARE="["
if [ $(docker inspect -f '{{.State.Running}}' od-server) ]; then
    export CONNECTED_HARDWARE=$CONNECTED_HARDWARE"\"OD-DOCKER-CONTAINER\""
fi
#CONNECTED_HARDWARE=$CONNECTED_HARDWARE"\"CAMERA\""
CONNECTED_HARDWARE=$CONNECTED_HARDWARE"]"
export CONNECTED_HARDWARE
