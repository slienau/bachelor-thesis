# sample flows

## heartbeat

```JSON
[{"id":"e2a5d735.04f528","type":"tab","label":"Heartbeat","disabled":false,"info":""},{"id":"1c9a5e74.420bc2","type":"inject","z":"e2a5d735.04f528","name":"","topic":"","payload":"","payloadType":"date","repeat":"5","crontab":"","once":false,"onceDelay":0.1,"x":130,"y":80,"wires":[["e6aaf525.090b28"]]},{"id":"4df496de.1fe1c8","type":"mqtt out","z":"e2a5d735.04f528","name":"","topic":"/devices/","qos":"","retain":"","broker":"e45ff5b4.4cac58","x":340,"y":300,"wires":[]},{"id":"e6aaf525.090b28","type":"function","z":"e2a5d735.04f528","name":"get load","func":"osUtils = global.get('osUtils')\n\npayload = {\n    timestamp: msg.payload,\n    deviceName: 'raspi-01',\n    totalMem: osUtils.totalmem(),\n    freeMem: osUtils.freemem(),\n    cpuCount: osUtils.cpuCount(),\n    loadAvg1: osUtils.loadavg(1),\n    loadAvg5: osUtils.loadavg(5),\n    loadAvg15: osUtils.loadavg(15),\n}\n\nreturn {payload} ;","outputs":1,"noerr":0,"x":180,"y":160,"wires":[["58e3c993.026f38"]]},{"id":"58e3c993.026f38","type":"json","z":"e2a5d735.04f528","name":"","property":"payload","action":"","pretty":true,"x":230,"y":220,"wires":[["4df496de.1fe1c8"]]},{"id":"e45ff5b4.4cac58","type":"mqtt-broker","z":"","name":"","broker":"192.168.7.13","port":"1883","clientid":"raspi-01","usetls":false,"compatmode":true,"keepalive":"60","cleansession":true,"birthTopic":"","birthQos":"0","birthPayload":"","closeTopic":"","closeQos":"0","closePayload":"","willTopic":"","willQos":"0","willPayload":""}]
```

## MQTT -> execute

```JSON
[{"id":"63506699.512558","type":"tab","label":"MQTT -> exec","disabled":false,"info":""},{"id":"c907f4cb.a3cfe8","type":"mqtt in","z":"63506699.512558","name":"","topic":"/raspi-01/in","qos":"2","broker":"e45ff5b4.4cac58","x":110,"y":80,"wires":[["d7e6b7a2.29f908","fcf995c4.34ba38"]]},{"id":"d7e6b7a2.29f908","type":"debug","z":"63506699.512558","name":"","active":true,"tosidebar":true,"console":false,"tostatus":false,"complete":"false","x":430,"y":80,"wires":[]},{"id":"fcf995c4.34ba38","type":"exec","z":"63506699.512558","command":"","addpay":true,"append":"","useSpawn":"false","timer":"10","oldrc":false,"name":"","x":150,"y":160,"wires":[["4b4f86c1.561298","d7e6b7a2.29f908"],["d7e6b7a2.29f908","6efee3b3.22580c"],[]]},{"id":"4b4f86c1.561298","type":"mqtt out","z":"63506699.512558","name":"","topic":"/raspi-01/out","qos":"","retain":"","broker":"e45ff5b4.4cac58","x":430,"y":180,"wires":[]},{"id":"6efee3b3.22580c","type":"mqtt out","z":"63506699.512558","name":"","topic":"/raspi-01/err","qos":"","retain":"","broker":"e45ff5b4.4cac58","x":430,"y":240,"wires":[]},{"id":"e45ff5b4.4cac58","type":"mqtt-broker","z":"","name":"","broker":"192.168.7.13","port":"1883","clientid":"raspi-01","usetls":false,"compatmode":true,"keepalive":"60","cleansession":true,"birthTopic":"","birthQos":"0","birthPayload":"","closeTopic":"","closeQos":"0","closePayload":"","willTopic":"","willQos":"0","willPayload":""}]
```

## webcam -> object detection

```JSON
[{"id":"51736fa9.f2588","type":"tab","label":"Webcam Object Detection","disabled":false,"info":""},{"id":"286cba7e.72f386","type":"http in","z":"51736fa9.f2588","name":"","url":"/image","method":"get","upload":false,"swaggerDoc":"","x":130,"y":140,"wires":[["4b5c4c4a.7a0254"]]},{"id":"4b5c4c4a.7a0254","type":"file in","z":"51736fa9.f2588","name":"","filename":"/tmp/image.jpg","format":"","sendError":true,"x":340,"y":140,"wires":[["1fc97efa.d3a901"]]},{"id":"1fc97efa.d3a901","type":"change","z":"51736fa9.f2588","name":"Set Headers","rules":[{"t":"set","p":"headers","pt":"msg","to":"{}","tot":"json"},{"t":"set","p":"headers.content-type","pt":"msg","to":"image/jpeg","tot":"str"}],"action":"","property":"","from":"","to":"","reg":false,"x":570,"y":140,"wires":[["67acde67.0be05"]]},{"id":"67acde67.0be05","type":"http response","z":"51736fa9.f2588","name":"","x":730,"y":140,"wires":[]},{"id":"bef3158d.6e1f58","type":"inject","z":"51736fa9.f2588","name":"","topic":"","payload":"","payloadType":"str","repeat":"","crontab":"","once":false,"onceDelay":"","x":140,"y":280,"wires":[["8371d0b4.d97de"]]},{"id":"8371d0b4.d97de","type":"exec","z":"51736fa9.f2588","command":"fswebcam","addpay":false,"append":"-i 0 -r 1280x720 --no-banner -","useSpawn":"false","timer":"","oldrc":false,"name":"","x":310,"y":280,"wires":[["26f0836e.fb5ccc","fde33495.2f5b68"],[],[]]},{"id":"b1d96919.058c18","type":"http request","z":"51736fa9.f2588","name":"","method":"POST","ret":"bin","paytoqs":false,"url":"http://dsl-mbp:6001/object-detection/detect-image","tls":"","proxy":"","authType":"","x":370,"y":360,"wires":[["e5988319.f7e9b"]]},{"id":"df7f7ddb.d03a9","type":"http in","z":"51736fa9.f2588","name":"","url":"/image_new","method":"get","upload":false,"swaggerDoc":"","x":140,"y":200,"wires":[["7da74a26.852bb4"]]},{"id":"7da74a26.852bb4","type":"file in","z":"51736fa9.f2588","name":"","filename":"/tmp/image_new.jpg","format":"","sendError":true,"x":360,"y":200,"wires":[["de6718f9.a095c8"]]},{"id":"de6718f9.a095c8","type":"change","z":"51736fa9.f2588","name":"Set Headers","rules":[{"t":"set","p":"headers","pt":"msg","to":"{}","tot":"json"},{"t":"set","p":"headers.content-type","pt":"msg","to":"image/jpeg","tot":"str"}],"action":"","property":"","from":"","to":"","reg":false,"x":570,"y":200,"wires":[["4f86f5d9.3e66ac"]]},{"id":"4f86f5d9.3e66ac","type":"http response","z":"51736fa9.f2588","name":"","x":730,"y":200,"wires":[]},{"id":"26f0836e.fb5ccc","type":"file","z":"51736fa9.f2588","name":"","filename":"/tmp/image.jpg","appendNewline":false,"createDir":false,"overwriteFile":"true","encoding":"none","x":560,"y":260,"wires":[[]]},{"id":"e5988319.f7e9b","type":"file","z":"51736fa9.f2588","name":"","filename":"/tmp/image_new.jpg","appendNewline":false,"createDir":false,"overwriteFile":"true","encoding":"none","x":600,"y":360,"wires":[[]]},{"id":"fde33495.2f5b68","type":"function","z":"51736fa9.f2588","name":"set content type","func":"msg.headers = {};\nmsg.headers['content-type'] = 'image/jpeg';\nreturn msg;","outputs":1,"noerr":0,"x":180,"y":360,"wires":[["b1d96919.058c18"]]}]
```

## Sensordata

```JSON
[{"id":"e4b8f1c3.a29c3","type":"tab","label":"Sensordata","disabled":false,"info":""},{"id":"37b135ad.f3f05a","type":"inject","z":"e4b8f1c3.a29c3","name":"interval","topic":"","payload":"","payloadType":"date","repeat":"","crontab":"","once":false,"onceDelay":0.1,"x":110,"y":100,"wires":[["578057.9b155fa8"]]},{"id":"578057.9b155fa8","type":"function","z":"e4b8f1c3.a29c3","name":"generate sensor data","func":"// 1 MB\nconst data = 'x'.repeat(1*1024*1024)\n// msg.payload = Buffer.byteLength(text2, 'utf8');\n\nlet randomString = ''\n\nfor (i = 0; i < 1024; i++) {\n  // each iteration generates 8 bytes\n  randomString += Math.random().toString(36).substring(2, 10)\n}\n\nmsg.payload = {\n    messageId: Math.random().toString(36).substring(2, 10), //TODO: use UUID\n    sensorId: 'sensor-01',\n    origin: 'node-01',\n    timeMeasurement: new Date(),\n    datasize: Buffer.byteLength(randomString, 'utf8'),\n    sensordata: randomString,\n}\n\nreturn msg;","outputs":1,"noerr":0,"x":300,"y":100,"wires":[["fbf0984d.931e48"]]},{"id":"54a2197.f96b7e8","type":"function","z":"e4b8f1c3.a29c3","name":"process sensor data","func":"const searchString = '123'\n\nlet status = 'GOOD'\n\nif (msg.payload.sensordata.indexOf(searchString) !== -1) {\n    status = 'BAD'\n}\n\nmsg.payload.status = status\nmsg.payload.sensordata = undefined\nmsg.payload.datasize = undefined\n\n// Simulated processing\nlet random = Math.random()\nfor (i = 0; i < 10000; i++) {\n  random += Math.random()\n}\n\nreturn msg;","outputs":1,"noerr":0,"x":600,"y":440,"wires":[["9a5b83e5.49191"]]},{"id":"da865a8c.ef9ec8","type":"debug","z":"e4b8f1c3.a29c3","name":"","active":true,"tosidebar":true,"console":false,"tostatus":false,"complete":"payload","targetType":"msg","x":790,"y":260,"wires":[]},{"id":"364d4a43.4d48f6","type":"function","z":"e4b8f1c3.a29c3","name":"status message","func":"const {status, timeTotal, timeProcessingTotal, timeTransferTotal} = msg.payload\n\nmsg.payload = `status is ${status}; \ntotal time:\n  ${timeTotal}ms;\nprocessing time:\n  ${timeProcessingTotal}ms;\ntransfer time:\n  ${timeTransferTotal}ms\n`\n\nreturn msg;","outputs":1,"noerr":0,"x":600,"y":260,"wires":[["da865a8c.ef9ec8"]]},{"id":"c40c0a87.f00948","type":"comment","z":"e4b8f1c3.a29c3","name":"Fog-node 1: Has sensor connected, collects sensor data and sends it via HTTP to other node for processing","info":"","x":410,"y":160,"wires":[]},{"id":"78639ca.e673064","type":"comment","z":"e4b8f1c3.a29c3","name":"Fog-node 2: No sensor connected, receives sensor data, processes sensor data, sends back the result via HTTP","info":"","x":420,"y":500,"wires":[]},{"id":"5d4ae134.6b445","type":"comment","z":"e4b8f1c3.a29c3","name":"Fog-node 1: Receives the result from Fog-node 2, does something with the result","info":"","x":320,"y":320,"wires":[]},{"id":"d5e40204.df7ab","type":"function","z":"e4b8f1c3.a29c3","name":"add timeResultReceived","func":"const payload = msg.payload\n\npayload.timeResultReceived = new Date()\n\nreturn msg;","outputs":1,"noerr":0,"x":150,"y":260,"wires":[["875790f7.87791"]]},{"id":"875790f7.87791","type":"function","z":"e4b8f1c3.a29c3","name":"calculate time stats","func":"const payload = msg.payload\n\n// Total time\npayload.timeTotal = \n(new Date(payload.timeResultReceived))\n- (new Date(payload.timeStart))\n\n// Processing time\npayload.timeProcessingTotal = \n(new Date(payload.timeProcessingEnd)) \n- (new Date(payload.timeProcessingStart))\n\n// Transfer time\nconst timeTransfer1 = \n(new Date(payload.timeProcessingStart))\n- (new Date(payload.timeStart))\n\nconst timeTransfer2 =\n(new Date(payload.timeResultReceived))\n- (new Date(payload.timeProcessingEnd))\n\npayload.timeTransferTotal = timeTransfer1 + timeTransfer2\n\nreturn msg;","outputs":1,"noerr":0,"x":390,"y":260,"wires":[["364d4a43.4d48f6"]]},{"id":"ee5e1581.901408","type":"http request","z":"e4b8f1c3.a29c3","name":"","method":"POST","ret":"obj","paytoqs":false,"url":"localhost:1880/sensordata","tls":"","proxy":"","authType":"","x":750,"y":100,"wires":[["d5e40204.df7ab"]]},{"id":"9a5b83e5.49191","type":"function","z":"e4b8f1c3.a29c3","name":"add timeProcessingEnd","func":"msg.payload.timeProcessingEnd = new Date()\n\nreturn msg;","outputs":1,"noerr":0,"x":850,"y":440,"wires":[["3cf02dcb.aa4962"]]},{"id":"f85e1bf9.9111b8","type":"http in","z":"e4b8f1c3.a29c3","name":"","url":"/sensordata","method":"post","upload":false,"swaggerDoc":"","x":130,"y":440,"wires":[["2b765ad5.788a16"]]},{"id":"fbf0984d.931e48","type":"function","z":"e4b8f1c3.a29c3","name":"add timeStart","func":"msg.payload.timeStart = new Date()\n\nreturn msg;","outputs":1,"noerr":0,"x":520,"y":100,"wires":[["ee5e1581.901408"]]},{"id":"3cf02dcb.aa4962","type":"http response","z":"e4b8f1c3.a29c3","name":"","statusCode":"","headers":{},"x":1070,"y":440,"wires":[]},{"id":"2b765ad5.788a16","type":"function","z":"e4b8f1c3.a29c3","name":"add timeProcessingStart","func":"msg.payload.timeProcessingStart = new Date()\n\nreturn msg;","outputs":1,"noerr":0,"x":350,"y":440,"wires":[["54a2197.f96b7e8"]]}]
```