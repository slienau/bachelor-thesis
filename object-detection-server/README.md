# Object Detection Server

## Install

The runtime environment is available as a docker image.
Inside the `scripts/` folder you can find scripts for building the images.
These are available for `x86` CPU architectures, and for `arm` (which can be used on Raspberry Pis).
You have to build the docker images first before you can run the object detection server.

### Build the image

```bash
cd scripts
./build.sh
```

Executing the build script will create the docker image `object-detection-server`.
The image is built for the architecture where the script is executed (as long it is `x86` or `arm`).

## Run

### Run object detection server

```bash
cd scripts
./docker_run.sh
```

Server will be available at `localhost:6001`

### Run in development mode

In development mode, the `src/` dir will be mounted to the docker container, so local changes are detected and applied by flask inside of the container.

```bash
cd scripts
./docker_run_dev.sh
```

## API

### Endpoints

#### POST `/object-detection/detect-image`

Sending a HTTP POST request to `/object-detection/detect-image` with an image inside the body (e.g. `sample-images/image2.jpg`) will return the detected image in the response.

##### Request
|Type | Content |
|--- |--- |
| Header | `Content-Type: image/jpeg` |
| Body | Raw image data (original) |

##### Response
|Type |Content |
|--- |--- |
| Header | `Content-Type: image/jpeg` |
| Body | Raw image data (detected) |
