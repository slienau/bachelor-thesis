export PUBLIC_FQDN=dsl89.ddns.net
export PUBLIC_PORT=1880
export MQTT_SERVER=dslbabroker.westeurope.azurecontainer.io

CONNECTED_HARDWARE="["
if [ $(docker inspect -f '{{.State.Running}}' od-server 2>/dev/null) ]; then
    export CONNECTED_HARDWARE=$CONNECTED_HARDWARE"\"OD-DOCKER-CONTAINER\""
fi
#CONNECTED_HARDWARE=$CONNECTED_HARDWARE"\"CAMERA\""
CONNECTED_HARDWARE=$CONNECTED_HARDWARE"]"
export CONNECTED_HARDWARE
