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

## MQTT commands

Can be send to `/devices/{deviceName}/commands/in/{COMMAND}`

| Command | Explanation | Message Content | Example payload |
|--- |--- |--- |---|
| `ping` | Pings destination host | ping destination address | `raspi-02` |
| `benchmark_cpu` | Runs benchmark on CPU (outputs execution time in seconds - the lower, the better) | anything | `benchmark` |
| `iperf3` | Measures the bandwidth to/from destination | destination address | `raspi-02` |
| `sysinfo` | Sends system information | anything | `raspi-02` |

The output will be send to the MQTT topic `/devices/dsl-mbp.lan/commands/out/{COMMAND}`
