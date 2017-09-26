# A notebook for classifying Set card game images.
#
# A lot of the code is based on chapter 5 of "Deep Learning with Python" by
# Francois Chollet.

'''
!pip3 install -U numpy tensorflow-gpu keras
!pip3 install pillow h5py
!pip3 install keras-vis # https://github.com/raghakot/keras-vis
'''

# The dataset consists of JPEG images of size 267 (height) by 171 (width) in a directory
# _data/train-v2/labelled/<label>_
# Before using this file, run _create-datasets-for-cnn-.py_ first.

# To start with we are going to classify the color of the card (green, red, or purple),
# which is not as easy as it sounds since lighting have a very big effect on the pixel
# color values.

# Change this to one of 'color', 'number', 'shape', 'shading', 'all',
# or 'shape-small' to use the artificially small training dataset to observe overfitting
# (in this case remove the dropout layer from the model too).
attribute = 'color'

# Count the images in each train/validation/test split
import os
base_dir = f'data/{attribute}'
train_dir = os.path.join(base_dir, 'train')
validation_dir = os.path.join(base_dir, 'validation')
test_dir = os.path.join(base_dir, 'test')
for dir in (train_dir, validation_dir, test_dir):
    labels = os.listdir(dir)
    labels.sort()
    num_labels = len(labels)
    for label in labels:
        print('total ' + dir + ', ' + label, len(os.listdir(os.path.join(dir, label))))
print('total labels: ', num_labels)

import glob
training_image_count = len(glob.glob(os.path.join(train_dir, "*", "*.jpg")))
validation_image_count = len(glob.glob(os.path.join(validation_dir, "*", "*.jpg")))
test_image_count = len(glob.glob(os.path.join(test_dir, "*", "*.jpg")))
print('total training images: ', training_image_count)
print('total validation images: ', validation_image_count)
print('total test images: ', test_image_count)
        
# Build a convnet
# images are 267 (height) by 171 (width), but we resize to 150 x 150
from keras import layers
from keras import models
from keras import backend as K
K.clear_session() # reset graph so new one has predicatable names for TF export
model = models.Sequential()
model.add(layers.Conv2D(32, (3, 3), activation='relu',
                        input_shape=(150, 150, 3)))
model.add(layers.MaxPooling2D((2, 2)))
model.add(layers.Conv2D(64, (3, 3), activation='relu'))
model.add(layers.MaxPooling2D((2, 2)))
model.add(layers.Conv2D(128, (3, 3), activation='relu'))
model.add(layers.MaxPooling2D((2, 2)))
model.add(layers.Conv2D(128, (3, 3), activation='relu'))
model.add(layers.MaxPooling2D((2, 2)))
model.add(layers.Flatten())
model.add(layers.Dropout(0.5))
model.add(layers.Dense(512, activation='relu'))
model.add(layers.Dense(num_labels, activation='softmax'))

model.summary()

model.compile(optimizer='rmsprop',
              loss='categorical_crossentropy',
              metrics=['accuracy'])

# Create generators to read train and validation images.
# Also do some pre-processing to resize images.
from keras.preprocessing.image import ImageDataGenerator
# All images will be rescaled by 1./255
train_datagen = ImageDataGenerator(rescale=1./255)
validation_datagen = ImageDataGenerator(rescale=1./255)
train_generator = train_datagen.flow_from_directory(
        train_dir,
        # All images will be resized to 150x150
        target_size=(150, 150),
        batch_size=20)
validation_generator = validation_datagen.flow_from_directory(
        validation_dir,
        target_size=(150, 150),
        batch_size=20)

for data_batch, labels_batch in train_generator:
  print('data batch shape:', data_batch.shape)
  print('labels batch shape:', labels_batch.shape)
  break

# Fit a model
history = model.fit_generator(
      train_generator,
      steps_per_epoch=training_image_count // 20 + 1,
      epochs=100,
      validation_data=validation_generator,
      validation_steps=validation_image_count // 20 + 1)
model.save(f'set_{attribute}s_1.h5')

# Plot loss and accuracy during training 
# (training values as dots, validation values as solid lines)
import matplotlib.pyplot as plt

def smooth_curve(points, factor=0.8):
    smoothed_points = []
    for point in points:
        if smoothed_points:
            previous = smoothed_points[-1]
            smoothed_points.append(previous * factor + point * (1 - factor))
        else:
            smoothed_points.append(point)
    return smoothed_points

acc = history.history['acc']
val_acc = history.history['val_acc']
loss = history.history['loss']
val_loss = history.history['val_loss']
epochs = range(len(acc))
plt.ylim(0.8, 1.0)
plt.plot(epochs, acc, 'bo')
plt.plot(epochs, smooth_curve(val_acc), 'b')
plt.title('Training and validation accuracy')
plt.figure()
plt.plot(epochs, loss, 'bo')
plt.plot(epochs, smooth_curve(val_loss), 'b')
plt.title('Training and validation loss')
plt.show()

# Load a previously-saved model
#from keras.models import load_model
#model = load_model(f'set_{attribute}s_1.h5')

# Evaluate the model on test data
test_datagen = ImageDataGenerator(rescale=1./255)
test_generator = test_datagen.flow_from_directory(
        test_dir,
        target_size=(150, 150),
        batch_size=20)
test_loss, test_acc = model.evaluate_generator(test_generator, test_image_count // 20 + 1)
print('test acc:', test_acc)

# Test on 15 hand-picked images
test_dir2 = os.path.join(f'data/{attribute}-test')
test_generator2 = test_datagen.flow_from_directory(
        test_dir2,
        target_size=(150, 150),
        batch_size=15,
        classes=labels) # need to specify since test data may not have all classes
test_loss, test_acc = model.evaluate_generator(test_generator2, steps=1)
print('test acc:', test_acc)

# Convert to a TensorFlow model

import tensorflow as tf
def freeze_session(session, keep_var_names=None, output_names=None, clear_devices=True):
    """
    Freezes the state of a session into a prunned computation graph.

    Creates a new computation graph where variable nodes are replaced by
    constants taking their current value in the session. The new graph will be
    prunned so subgraphs that are not neccesary to compute the requested
    outputs are removed.
    @param session The TensorFlow session to be frozen.
    @param keep_var_names A list of variable names that should not be frozen,
                          or None to freeze all the variables in the graph.
    @param output_names Names of the relevant graph outputs.
    @param clear_devices Remove the device directives from the graph for better portability.
    @return The frozen graph definition.
    """
    from tensorflow.python.framework.graph_util import convert_variables_to_constants
    graph = session.graph
    with graph.as_default():
        freeze_var_names = list(set(v.op.name for v in tf.global_variables()).difference(keep_var_names or []))
        output_names = output_names or []
        output_names += [v.op.name for v in tf.global_variables()]
        input_graph_def = graph.as_graph_def()
        if clear_devices:
            for node in input_graph_def.node:
                node.device = ""
        frozen_graph = convert_variables_to_constants(session, input_graph_def,
                                                      output_names, freeze_var_names)
        return frozen_graph

from keras import backend as K
frozen_graph = freeze_session(K.get_session(), output_names=[model.output.op.name])

tf.train.write_graph(frozen_graph, "tf", f'set_{attribute}.pb', as_text=False)
with open(f'tf/set_{attribute}.txt', 'w') as f:
  f.writelines([label + '\n' for label in labels])

print('input operation name: ', model.input.op.name)
print('output operation name: ', model.output.op.name)

# Use Tensorboard to view the graph
tf.summary.FileWriter("logs", frozen_graph).close()
# Run the following in the CDSW terminal, then open the web UI by clicking
# on the grid icon in the upper right hand corner, and clicking "TensorFlow"
# tensorboard --logdir=logs --port=$CDSW_PUBLIC_PORT

# Visualization!

def image_to_tensor(img_path):
  from keras.preprocessing import image
  import numpy as np
  img = image.load_img(img_path, target_size=(150, 150))
  img_tensor = image.img_to_array(img)
  img_tensor = np.expand_dims(img_tensor, axis=0)
  img_tensor /= 255.
  return img_tensor

# keras-vis
# https://github.com/raghakot/keras-vis/blob/master/examples/mnist/attention.ipynb
from vis.visualization import visualize_saliency
from vis.visualization import visualize_activation
from vis.utils import utils
from keras import activations

# Utility to search for layer index by name. 
# Alternatively we can specify this as -1 since it corresponds to the last layer.
layer_idx = -1 # utils.find_layer_idx(model, 'preds')

# Swap softmax with linear
model.layers[layer_idx].activation = activations.linear
model = utils.apply_modifications(model)

# Create table showing test images and corresponding activations.
# The interesting point is that the edges of shapes are activated,
# even for solid shapes since the edge is the most reliable way of
# telling what the color is. So looking at the edge regardless
# of the shape is a good strategy. The network learned this by itself.
import glob
test_images = glob.glob(os.path.join(test_dir2, "*", "*.jpg"))

import matplotlib.pyplot as plt
import numpy as np
for img_path in test_images:
  img_tensor = image_to_tensor(img_path)
  preds = model.predict(img_tensor)
  class_index = np.argmax(preds)
  f, ax = plt.subplots(1, 2)
  ax[0].imshow(img_tensor[0])
  grads = visualize_saliency(model, layer_idx, filter_indices=class_index, 
                             seed_input=img_tensor, backprop_modifier='guided')
  ax[1].imshow(grads, cmap='jet')
  plt.show()

# Activation maximization, or how each class is perceived by the network
for class_index in range(3):
  img = visualize_activation(model, layer_idx, filter_indices=class_index, input_range=(0., 1.))
  plt.figure()
  plt.title('Networks perception of {}'.format(labels[class_index]))
  plt.imshow(img[..., 0])
  plt.show()
