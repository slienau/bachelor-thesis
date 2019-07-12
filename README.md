# Object Detection Server

## Install

### Linux / Mac

The whole runtime environment is available as a docker image. To build it, run:

```bash
cd docker
./build.sh
```

## API

### Start API server (Docker container)

```bash
cd docker
./run.sh
```

Server will be available at `localhost:6001`

### Endpoints

#### POST `/object-detection/detect-image`

|--- |--- |
|--- |--- |
| Request Header | `Content-Type: image/jpeg` |
| Request Body | Raw image data (original) |
| Response Body | Raw image data (processed) |
