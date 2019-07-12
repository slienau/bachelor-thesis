from flask import Flask, request, Response
from detect_image import process_image_from_bytes
from datetime import datetime

app = Flask(__name__)


@app.route('/object-detection/detect-image', methods=['POST'])
def detect_image():
    print(datetime.now(), 'got request for', request.url, 'content-type', request.content_type)

    if request.content_type != "image/jpeg":
        print(datetime.now(), 'wrong content type')
        return Response(response='wrong content type', status=400, mimetype="text/plain")

    print(datetime.now(), 'starting to process image')
    start_time = datetime.now()
    image_bytes_new = process_image_from_bytes(request.data)
    end_time = datetime.now()
    print(datetime.now(), 'image processed. duration:', end_time - start_time)

    return Response(response=image_bytes_new, status=200, mimetype="image/jpeg")


if __name__ == "__main__":
    app.run(debug=True, host='0.0.0.0', port=6001)
