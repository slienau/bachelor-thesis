# node-red

## Docker

### Build images

The `build_all.sh` script inside the `scripts/` folder builds four docker images:

1. `node-red`
2. `node-red:slim`
3. `node-red:arm`
4. `node-red:arm-slim`

`latest` and `slim` are based on the [official node docker image](https://hub.docker.com/_/node), while `arm` is based on the [`arm32v7/node`](https://hub.docker.com/r/arm32v7/node) docker image.  
`slim` images are smaller but contain fewer standard packages.

### Run containers

First of all, `cd` into the folder `scripts/`. Set environment variables in `env.sh`, then run the desired script, e.g. `./docker_run.sh` for the initial creation of a container based on the .
