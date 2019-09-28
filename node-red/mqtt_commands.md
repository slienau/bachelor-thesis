# MQTT commands

Commands can be send to `/devices/{deviceName}/commands/in/{COMMAND}`.\
The output will be send to the MQTT topic `/devices/{deviceName}/commands/out/{COMMAND}`.

The Orchestrator is using this interface to communicate with the nodes.

| Command | Explanation | Message Content |
|--- |--- |--- |
| `ping` | Pings destination host | destination address |
| `benchmark_cpu` | Runs benchmark on CPU (outputs execution time in seconds - the lower, the better) | - |
| `sysinfo` | Sends system information | - |
| `bandwidth` | Measures the link bandwidth from device to destination via HTTP | destination address |
| `iperf3` (deprecated) | Measures the link bandwidth from device to destination via TCP | destination address |

## Command input and output examples

### sysinfo

`/devices/raspi-01/commands/in/sysinfo`:

```json
---
```

`/devices/raspi-01/commands/out/sysinfo`:

```json
{
  "timestamp" : "2019-09-28T16:16:55.047Z",
  "deviceName" : "raspi-01",
  "arch" : "arm",
  "totalMem" : 975.62109375,
  "freeMem" : 182.5078125,
  "cpuCount" : 4,
  "loadAvg1" : 0,
  "loadAvg5" : 0,
  "loadAvg15" : 0,
  "connectedHardware" : [ "CAMERA" ],
  "totalDisk" : 28.991924285888672,
  "freeDisk" : 26.06018829345703
}
```

### bandwidth

`/devices/raspi-01/commands/in/bandwidth`:

```json
{
  "destination" : "127.0.0.1:1880",
  "size" : 10240
}
```

`/devices/raspi-01/commands/out/bandwidth`:

```json
{
  "source" : "raspi-01",
  "destination" : "127.0.0.1:1880",
  "time" : 1009,
  "size" : 10485760,
  "mbitPerSecond" : 83.13783944499505
}
```

### benchmark_cpu

`/devices/raspi-01/commands/in/benchmark_cpu`:

```json
---
```

`/devices/raspi-01/commands/out/benchmark_cpu`:

```json
{
  "cpuScore" : 612.6587551999412
}
```

### ping

`/devices/raspi-01/commands/in/ping`:

```json
www.google.com
```

`/devices/raspi-01/commands/out/ping`:

```json
{
  "source" : "raspi-01",
  "destination" : "www.google.com",
  "time" : "20.091"
}
```
