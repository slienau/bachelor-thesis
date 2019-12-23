ARG NODE_VERSION=12
ARG BASE_NODE_IMAGE=node
ARG BASE_IMAGE_SUFFIX="-stretch-slim"
FROM ${BASE_NODE_IMAGE}:${NODE_VERSION}${BASE_IMAGE_SUFFIX}

# install aditional software
RUN apt-get update \
&& apt-get install -y --no-install-recommends iputils-ping iperf3 sysbench \
# cleanup
&& rm -rf /var/lib/apt/lists/* \
&& apt-get -y autoremove

# Home directory for Node-RED application source code.
WORKDIR /usr/src/node-red

# package.json contains Node-RED NPM module and node dependencies
COPY src/ /usr/src/node-red/
RUN npm install --no-optional

# copy data
WORKDIR /data
COPY data/ /data/
RUN npm install

# Add node-red user so we aren't running as root.
# RUN useradd --home-dir /usr/src/node-red --no-create-home -p "$(openssl passwd -1 node-red)" node-red \
#   && mkdir /data \
#   && chown -R node-red:node-red /data \
#   && chown -R node-red:node-red /usr/src/node-red \
#   && usermod -a -G video node-red
#USER node-red

# User configuration directory volume
EXPOSE 1880

# Environment variable holding file path for flows configuration
ENV FLOWS=flows.json
ENV NODE_PATH=/usr/src/node-red/node_modules:/data/node_modules

WORKDIR /usr/src/node-red

CMD ["bash", "/data/docker-entrypoint.sh"]