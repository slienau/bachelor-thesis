#!/bin/bash
MQTT_SERVER=$MQTT_SERVER
sed -i -e "s/<###NODE_NAME###>/$HOSTNAME/g" /data/flows.json
sed -i -e "s/<###MQTT_SERVER###>/$MQTT_SERVER/g" /data/flows.json
cd /usr/src/node-red
npm start -- --userDir /data