export DOCKER_CONTAINER_NAME=iperf3-server
export DOCKER_IMAGE=iperf3-server
export DOCKER_REGISTRY=dsl-mbp

IS_ARM=FALSE
if [[ "$(uname -m)" == "arm"* ]]; then
    IS_ARM=TRUE
fi
export IS_ARM