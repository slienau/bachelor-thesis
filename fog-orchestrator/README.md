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
