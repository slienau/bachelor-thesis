# Fog orchestrator

The Fog orchestrator collects data about every fog nodes system load and about the overall network quality.
Based on that it calculates and deploys optimal deployments to ensure best QoS.

## Setup


### Set fields
Setup is done in the `NodeRedOrchestrator` class, since this contains the main method of the Orchestrator.

1. Set the `application` field to the desired application (e.g. object detection)
2. Set `MQTT_BROKER` to the correct address.
This broker should also be used on the fog nodes where they send their heartbeats.


### Start flow database

Start a Node-RED container on `localhost` on port `2880`.
The `node-red` image is the same one that is used on the fog nodes (see folder `../node-red/`)

```bash
docker run -it -d
--restart=unless-stopped \
--name nodered_flow_database \
-v nodered_flow_database:/data \
-p 2880:1880 \
node-red
```

The fog orchestrator will query this instance to get Node-RED flows in order to deploy/distribute these flows to other fog nodes.
These must comply with the `Application` model set in the previous step.

### Import flows to database
Sample flows which contain the _Object Detection Application_ as well as the _Sensor Network Application_ can be found in the file `flow_database.json` inside of this folder.
These can be imported via the Node-RED Web interface on [http://localhost:2880/](http://localhost:2880/) (after starting the container in the previous step)



## Run

The main class of the Orchestrator is the `NodeRedOrchestrator` class inside the package `de.tuberlin.aot.thesis.slienau.orchestrator`.
If you're using this project in a IDE, simply run this class.
If not, build the `.jar` file first by using _Maven_ and execute it thereafter:

```
mvn package
java -jar target/fog-orchestrator-0.0.1-jar-with-dependencies.jar
```

