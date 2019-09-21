from flask import Flask, request, Response
from detect_image import ObjectDetector
from datetime import datetime

app = Flask(__name__)


@app.route('/object-detection/detect-image', methods=['POST'])
def detect_image():
    print(datetime.now(), 'got request for', request.url, 'content-type', request.content_type)

    if request.content_type != "image/jpeg":
        print(datetime.now(), 'wrong content type')
        return Response(response='wrong content type', status=400, mimetype="text/plain")

    image_bytes_new = object_detector.detect(request.data)

    return Response(response=image_bytes_new, status=200, mimetype="image/jpeg")


object_detector = ObjectDetector()


if __name__ == "__main__":
    app.run(debug=True, host='0.0.0.0', port=6001)
