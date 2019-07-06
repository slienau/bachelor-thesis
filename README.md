# node-red

## Docker

### Build images

The `build.sh` script builds four docker images:

1. `node-red`
2. `node-red:slim`
3. `node-red:arm`
4. `node-red:arm-slim`

`latest` and `slim` are based on the [official node docker image](https://hub.docker.com/_/node), while `arm` is based on the [`arm32v7/node`](https://hub.docker.com/r/arm32v7/node) docker image.  
`slim` images are smaller but contain fewer standard packages.

### Run containers

Set environment variables in `node_red.sh`, then run the script. The following commands are supported:

- `run`
- `stop`
- `start`
- `restart`
- `remove`
- `attach`
- `bash`

E.g. to start the container initially, run `./node_red.sh run`
