# node-red

## Setup

### Build the image

```bash
cd scripts
./build.sh
```

Executing the build script will create the docker image `node-red`.\
The image is built for the architecture where the script is executed (as long it is `x86` or `arm`).\
If architecture is non-arm, the base image is the [official node docker image](https://hub.docker.com/_/node), while `arm` is based on the [`arm32v7/node`](https://hub.docker.com/r/arm32v7/node) docker image.  

### Set environment variables

Create a copy of the file `../env_default.sh` and place it inside `../env.sh`.
Next, edit the variables:

| Name | Content |
|--- |--- |
|`PUBLIC_FQDN` | The public DNS entry (or IP address) where this Node-RED instance can be reached |
|`PUBLIC_PORT`| The public port where this Node-RED instance can be reached |
|`MQTT_SERVER` | Address of the MQTT server used for communication between Node-RED and the Orchestrator |
|`CPU_LIMIT` | Max. amount of CPU cores |

Furthermore, there is the variable `CONNECTED_HARDWARE` which is a JSON array containing strings of connected hardware modules.
By default, it is set to `[]`, and if the `od-server` (object detection server) container is running, it will be set to `["OD-DOCKER-CONTAINER"]` automatically.
This may be changed of course.\
For instance, for the object detection application, the module `webapp` needs the hardware module `CAMERA` to be connected.
Therefore, `CONNECTED_HARDWARE` should be set to `["CAMERA"]`.\
Multiple hardware modules can be set like this: `["CAMERA", "TEMPERATURE"]`.

## Run

### Node-RED

After setting the environment variables, the Node-RED software can be started via the `docker run` command.
Because there are a number of option passed to the docker command, you should use the provided script which will start the docker container by using the environment variables set in the previous step.

```bash
cd scripts
./docker_run.sh
```

To attach or to remove the container, run the `docker_attach.sh` or `docker_remove.sh` script, respectively.

#### Initial flow

Initially, the Node-RED instance will contain the *Monitoring* flow which sends a heartbeat message to the MQTT broker every two seconds.\
Furthermore, the flow is able to receive serveral commands via MQTT. These are explained in the file `mqtt_commands.md`.

### Object Detection Server

If this Node-RED instance should be able to run the `detector` flow of the object detection application, the `object-detection-server` docker image must run as well on this node.\
Further information on this can be found in `../object-detection-server/README.md`
