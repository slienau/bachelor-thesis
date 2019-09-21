FROM arm32v7/debian:buster as builder

ENV OD_MODEL=ssd_mobilenet_v1_coco_2018_01_28

WORKDIR /opt

RUN apt-get update \
&& apt-get -y upgrade \
&& apt-get install -y git protobuf-compiler curl \
&& git clone https://github.com/tensorflow/models \
&& cd /opt/models/research \
&& protoc object_detection/protos/*.proto --python_out=. \
&& mkdir -p /opt/graph_def \
&& cd /tmp \
&& curl -OL http://download.tensorflow.org/models/object_detection/${OD_MODEL}.tar.gz \
&& tar -xzf ${OD_MODEL}.tar.gz ${OD_MODEL}/frozen_inference_graph.pb \
&& cp -a ${OD_MODEL} /opt/graph_def/ \
&& ln -sf /opt/graph_def/${OD_MODEL}/frozen_inference_graph.pb /opt/graph_def/frozen_inference_graph.pb


FROM arm32v7/debian:buster-slim

COPY --from=builder /opt/graph_def /opt/graph_def/
COPY --from=builder /opt/models/research/object_detection /opt/models/research/object_detection/
COPY --from=builder /opt/models/research/slim /opt/models/research/slim/

RUN apt update \
&& apt-get install -y python3 python3-pip \
&& apt-get install -y --no-install-recommends python3-dev \
# tensorflow dependencies
# && apt-get install -y --no-install-recommends libatlas-base-dev \
&& apt-get install -y --no-install-recommends python3-pil python3-lxml python3-tk \
# additional python packages
&& apt-get install -y --no-install-recommends \
# python3-contextlib2 \
python3-numpy python3-h5py python3-grpcio python3-matplotlib python3-flask \
# cleanup
&& rm -rf /var/lib/apt/lists/* \
&& apt-get -y autoremove

RUN pip3 install --no-cache-dir --upgrade pip
RUN pip3 install --no-cache-dir https://www.piwheels.org/simple/tensorflow/tensorflow-1.13.1-cp37-none-linux_armv7l.whl
# RUN pip3 install --no-cache-dir Cython
# RUN pip3 install --no-cache-dir contextlib2
# RUN pip3 install --no-cache-dir jupyter
RUN pip3 install --no-cache-dir matplotlib
RUN pip3 install --no-cache-dir flask

COPY src/ /usr/src/object_detection_app/
WORKDIR /usr/src/object_detection_app

CMD [ "python3", "app.py"]
