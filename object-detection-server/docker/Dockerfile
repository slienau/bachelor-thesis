FROM python:3.7-slim as builder

ENV OD_MODEL=ssd_mobilenet_v1_coco_2018_01_28

WORKDIR /opt

RUN apt-get update \
&& apt-get install -y --no-install-recommends git protobuf-compiler curl \
&& git clone https://github.com/tensorflow/models \
&& cd /opt/models/research \
&& protoc object_detection/protos/*.proto --python_out=. \
&& mkdir -p /opt/graph_def \
&& cd /tmp \
&& curl -OL http://download.tensorflow.org/models/object_detection/${OD_MODEL}.tar.gz \
&& tar -xzf ${OD_MODEL}.tar.gz ${OD_MODEL}/frozen_inference_graph.pb \
&& cp -a ${OD_MODEL} /opt/graph_def/ \
&& ln -sf /opt/graph_def/${OD_MODEL}/frozen_inference_graph.pb /opt/graph_def/frozen_inference_graph.pb

FROM python:3.7-slim

COPY --from=builder /opt/graph_def /opt/graph_def/
COPY --from=builder /opt/models/research/object_detection /opt/models/research/object_detection/
COPY --from=builder /opt/models/research/slim /opt/models/research/slim/

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY src/ /usr/src/object_detection_app/
WORKDIR /usr/src/object_detection_app

CMD [ "python", "app.py"]
