# Fog orchestrator

The Fog orchestrator collects data about every fog nodes system load and about the overall network quality. Based on that it calculates and deploys optimal deployments to ensure best QoS.

## Setup

### Flow database

Start a node-RED container (based on the same image the fog nodes are using) on port `2880`. The fog orchestrator will query this instance to get node-RED flows in order to deploy/distribute these flows to other fog nodes.

```bash
docker run -it -d
--restart=unless-stopped \
--name nodered_flow_database \
-v nodered_flow_database:/data \
-p 2880:1880 \
node-red:slim
```

### Expose Docker API on fog nodes

Expose the Docker API on port `52376`

#### Raspberry Pi / Linux

Create file `/etc/systemd/system/docker.service.d/remote-api.conf` with the following content (replace `127.0.0.1` with the address of the network interface where the docker API should be exposed):

```bash
[Service]
ExecStart=
ExecStart=/usr/bin/dockerd -H tcp://127.0.0.1:52376 -H unix:///var/run/docker.sock
```

Restart Docker:

```bash
sudo systemctl daemon-reload
sudo systemctl restart docker
```

#### macOS

Using the tool `socat` (by running it inside its own docker container and passing the docker socket)

```bash
docker run -d --name socat-docker --restart unless-stopped -v /var/run/docker.sock:/var/run/docker.sock -p 52376:2375 bobrik/socat TCP4-LISTEN:2375,fork,reuseaddr UNIX-CONNECT:/var/run/docker.sock
```
