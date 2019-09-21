# Object Detection Server

## Install

The whole runtime environment is available as a docker image. There is a general image for Linux / Mac, and one for Raspberry Pi / ARM. You have to build the docker images first before you can run the object detection server.

### Linux / Mac

```bash
cd docker
./build.sh
```

### Raspberry Pi

```bash
cd docker/arm
./build.sh
```

## Run

### Run object detection server

```bash
cd docker
./run.sh
```

Server will be available at `localhost:6001`

### Development mode

In development mode, the `src/` dir will be mounted to the docker container, so local changes are detected and applied by flask inside of the container.

```bash
cd docker
./run.sh dev
```

## API

### Endpoints

#### POST `/object-detection/detect-image`

|--- |--- |
|--- |--- |
| Request Header | `Content-Type: image/jpeg` |
| Request Body | Raw image data (original) |
| Response Body | Raw image data (processed) |
