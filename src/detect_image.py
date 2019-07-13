import os
import sys
import io
from datetime import datetime

MODEL_BASE = '/opt/models/research'
sys.path.append(MODEL_BASE)
sys.path.append(MODEL_BASE + '/object_detection')
sys.path.append(MODEL_BASE + '/slim')

import numpy as np
import tensorflow as tf
from distutils.version import StrictVersion
from PIL import Image

from object_detection.utils import ops as utils_ops

from helpers import get_sample_image_bytes

if StrictVersion(tf.__version__) < StrictVersion('1.12.0'):
    raise ImportError('Please upgrade your TensorFlow installation to v1.12.*.')

from object_detection.utils import label_map_util
from object_detection.utils import visualization_utils as vis_util

PATH_TO_FROZEN_GRAPH = '/opt/graph_def/frozen_inference_graph.pb'
PATH_TO_LABELS = os.path.join(MODEL_BASE, 'object_detection/data/mscoco_label_map.pbtxt')


class ObjectDetector(object):
    def __init__(self):
        print(datetime.now(), 'ObjectDetection() - starting initialization')
        start_time = datetime.now()

        self.detection_graph = self._build_graph()
        self.sess = tf.compat.v1.Session(graph=self.detection_graph)

        label_map = label_map_util.load_labelmap(PATH_TO_LABELS)
        categories = label_map_util.convert_label_map_to_categories(label_map, max_num_classes=90, use_display_name=True)
        self.category_index = label_map_util.create_category_index(categories)

        all_tensor_names = {output.name for op in self.detection_graph.get_operations() for output in op.outputs}
        tensor_names = ['num_detections', 'detection_boxes', 'detection_scores', 'detection_classes', 'detection_masks']
        self.tensor_dict = {}
        for key in tensor_names:
            tensor_name = key + ':0'
            if tensor_name in all_tensor_names:
                self.tensor_dict[key] = self.detection_graph.get_tensor_by_name(tensor_name)

        if 'detection_masks' in self.tensor_dict:
            detection_boxes = tf.squeeze(self.tensor_dict['detection_boxes'], [0])
            detection_masks = tf.squeeze(self.tensor_dict['detection_masks'], [0])
            # Reframe is required to translate mask from box coordinates to image coordinates and fit the image size.
            real_num_detection = tf.cast(self.tensor_dict['num_detections'][0], tf.int32)
            self.detection_boxes = tf.slice(detection_boxes, [0, 0], [real_num_detection, -1])
            self.detection_masks = tf.slice(detection_masks, [0, 0, 0], [real_num_detection, -1, -1])

        self.image_tensor = self.detection_graph.get_tensor_by_name('image_tensor:0')

        # run the first detection because it takes more time than the following
        self.detect(get_sample_image_bytes())

        end_time = datetime.now()
        print(datetime.now(), 'ObjectDetection() - initialization complete. duration:', end_time - start_time)

    @staticmethod
    def _build_graph():
        detection_graph = tf.Graph()
        with detection_graph.as_default():
            od_graph_def = tf.compat.v1.GraphDef()
            with tf.io.gfile.GFile(PATH_TO_FROZEN_GRAPH, 'rb') as fid:
                serialized_graph = fid.read()
                od_graph_def.ParseFromString(serialized_graph)
                tf.import_graph_def(od_graph_def, name='')

        return detection_graph

    @staticmethod
    def _load_image_into_numpy_array(image):
        (im_width, im_height) = image.size
        return np.array(image.getdata()).reshape(
            (im_height, im_width, 3)).astype(np.uint8)

    def detect(self, image_bytes_in):
        print(datetime.now(), 'ObjectDetection() - starting to process image')
        start_time = datetime.now()
        image = Image.open(io.BytesIO(image_bytes_in))
        image_np = self._load_image_into_numpy_array(image)
        image_np_expanded = np.expand_dims(image_np, axis=0)

        tensor_dict = self.tensor_dict
    
        if 'detection_masks' in tensor_dict:
            detection_boxes = self.detection_boxes
            detection_masks = self.detection_masks
            detection_masks_reframed = utils_ops.reframe_box_masks_to_image_masks(
                detection_masks, detection_boxes, image.shape[1], image.shape[2])
            detection_masks_reframed = tf.cast(tf.greater(detection_masks_reframed, 0.5), tf.uint8)
            # Follow the convention by adding back the batch dimension
            tensor_dict['detection_masks'] = tf.expand_dims(
                detection_masks_reframed, 0)
    
        image_tensor = self.image_tensor

        output_dict = self.sess.run(tensor_dict, feed_dict={image_tensor: image_np_expanded})

        # all outputs are float32 numpy arrays, so convert types as appropriate
        output_dict['num_detections'] = int(output_dict['num_detections'][0])
        output_dict['detection_classes'] = output_dict[
            'detection_classes'][0].astype(np.int64)
        output_dict['detection_boxes'] = output_dict['detection_boxes'][0]
        output_dict['detection_scores'] = output_dict['detection_scores'][0]
        if 'detection_masks' in output_dict:
            output_dict['detection_masks'] = output_dict['detection_masks'][0]

        vis_util.visualize_boxes_and_labels_on_image_array(
            image_np,
            output_dict['detection_boxes'],
            output_dict['detection_classes'],
            output_dict['detection_scores'],
            self.category_index,
            instance_masks=output_dict.get('detection_masks'),
            use_normalized_coordinates=True,
            line_thickness=8)
        
        image_new = Image.fromarray(image_np)
        image_bytes_new = io.BytesIO()
        image_new.save(image_bytes_new, format=image.format)

        end_time = datetime.now()
        print('********************************************')
        print('**** detect() duration: ', end_time - start_time, '****')
        print('********************************************')
        return image_bytes_new.getvalue()
