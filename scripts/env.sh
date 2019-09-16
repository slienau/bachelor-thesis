export DOCKER_CONTAINER_NAME=nodered
export DOCKER_IMAGE=node-red
export DOCKER_VOLUME_NAME=nodered-data
export DOCKER_REGISTRY=dsl-mbp
export MQTT_SERVER=dslbabroker.westeurope.azurecontainer.io
export CONNECTED_HARDWARE="[]"
# export CONNECTED_HARDWARE="[\"CAMERA\",\"TEMPERATURE\"]"

IS_ARM=FALSE
if [[ "$(uname -m)" == "arm"* ]]; then
    IS_ARM=TRUE
fi
export IS_ARM
