export DOCKER_CONTAINER_NAME=nodered
export DOCKER_IMAGE=node-red
export DOCKER_VOLUME_NAME=nodered-data
export DOCKER_REGISTRY=dsl-mbp
export MQTT_SERVER=192.168.7.13

IS_ARM=FALSE
if [[ "$(uname -m)" == "arm"* ]]; then
    IS_ARM=TRUE
fi
export IS_ARM