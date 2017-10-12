# SET

Play [SET](https://www.setgame.com/) using image recognition. I've written this up as a
 blog post.
 
![Playing SET](animation.gif)

## Idea

Use image recognition algorithms that

1. Pick out individual cards from an image.
2. For each card, count the number of shapes, and detect the colour, shape and shading.

Finally finding _SETs_ within the image is the easy bit.

## Technique

I started by using basic image processing and machine learning techniques, before going
on to use Deep Learning. The basic approach serve as a baseline, and helped quantify
how much better Deep Learning can do.

## Data

I took photos of lots of SET cards under various lighting conditions. The cards were
on black backgrounds to make things easier. An obvious extension would be to try
different backgrounds at some point.

There are two training datasets.

### Dataset 1

The first dataset is found in _data/train_, and consists of 36 images of 9 cards.
Each image has cards of the same colour, and laid out in the same way.

The program `CreateTrainingSetV1` processes the raw training images and creates
a training set of images with one card in each image. See _data/train_out_.

The small size of the first dataset meant that colour detection in particular was
lacking, so I created a new, larger second dataset with more varied lighting conditions.

### Dataset 2

The _train-v2_ dataset is a collection of SET card images.

The _raw-archive_ directory contains original camera images. Each image is a 
photo of a board of 27 images, arranged in a 3 by 9 grid. There are three
boards, one contains all the SET cards with one shape, one with two shapes,
and one with three shapes.

The _raw-archive_ images are photos of the same boards taken at different times under
varying lighting conditions, from different angles, and with different cameras.

#### Process

1. Copy photos from the camera to a new _raw-archive/batch-nnnnn_ directory.
2. Visually inspect the images in Preview and make sure they all oriented correctly.
(Open the Inspector, and check the Orientation - it should be 1.) Rotate any that
are not the right way up.
3. Run `mkdir -p data/train-v2/raw-new; cp data/train-v2/raw-archive/batch-nnnnn/* data/train-v2/raw-new`
4. Run the following if files have an uppercase `.JPG` extension:
```
for file in data/train-v2/raw-new/*.JPG; do mv $file data/train-v2/raw-new/$(basename $file .JPG).jpg; done
```
5. Run `CheckRawTrainingImagesV2`. This will check that the images all have the correct orientation and the
individual cards can be detected.
6. If there are problematic images, then copy them to _raw-problem_. These will not be used, but
keep them as future versions of the code may be able to handle them.
7. Run `SortRawTrainingImagesV2`. This will programmatically detect the number of features on
each card so that it can sort the training boards with 1, 2, or 3 number cards. (Note 3 is called 0.)
8. Open each directory in _raw-sorted_ and visually check that each board is in the correct
directory. Move any that are not.
9. Run `CreateTrainingSetV2`. This will take each board in _raw-sorted_ and extract labelled
individual cards and store them in _raw-labelled_, then open a window showing each set of
cards. Visually inspect these to check they are correct. Move any images that are not.
10. Run 
```
mkdir -p data/train-v2/labelled/
rsync -a data/train-v2/raw-labelled/ data/train-v2/labelled/
rm -rf data/train-v2/raw-{new,sorted,labelled}
```
11. You can view all of the labelled images by running `ViewLabelledImagesV2`.

### Test Data

The test data is in _data/20170106_205743.jpg_, as well as _data/ad-hoc_ and 
_data/webcam_.

## Processing

The raw data is _preprocessed_ to get it into shape for training. The preprocessing
was discussed above, and the output is one card per image in labelled directories.

_Training_ is comprised of two parts: feature extraction from the images, and creating a
model from the features. Both steps are carried out by the `FeatureFinder` classes, which
use hand-crafted feature extractors, followed by k-nearest neighbors to do prediction.
(Note that model creation is not needed for kNN, since all the training data is used as the
model.) Furthermore, `FindCardNumberFeatures` does not even need a model since the
image processing can accurately count the number of shapes on a card.

Training is carried out by running `CreateTrainingDataV1`.

_Prediction_ (or inference) is the last step of the process, and uses the `FeatureFinder`
classes to recognize the cards in new or test images.

Prediction is carried out by the classes in `com.tom_e_white.set_game.predict`, including
`PlaySet` which takes an image (or a series of images from a webcam) and highlights the 
_SETs_ in it.

`PredictCardFeaturesOnTestData`
calculates the accuracy of predicting each feature (number, colour, shading, shape) for each
card in a test set. Here's a sample run:

```
FindCardNumberFeatures
Correct: 15
Total: 15
Accuracy: 100 percent
------------------------------------------
FindCardColourFeatures
Correct: 10
Total: 15
Accuracy: 66 percent
------------------------------------------
FindCardShadingFeatures
Incorrect, predicted 0 but was 1 for card 3
Correct: 14
Total: 15
Accuracy: 93 percent
------------------------------------------
FindCardShapeFeatures
Correct: 15
Total: 15
Accuracy: 100 percent
------------------------------------------
```

Notice that all but colour are predicted with high accuracy. I was initially very
surprised that detecting colour was so hard, but it turns out that the human eye
is very adept at colour detection: it is not just a question of measuring RGB
values in an image. Even controlling for lighting using HSB doesn't help much.
The main problem was that the training data in the first dataset was from a fairly
restricted range of lighting conditions, so it couldn't generalize well to the test
data. This is why I gathered the second dataset.

After training on the second dataset, the accuracy for colour prediction improves,
but it's still not as good as for the other features.

```
FindCardColourFeatures
Correct: 12
Total: 15
Accuracy: 80 percent
```

## Deep Learning

I trained a convolutional neural network on the second dataset (see _train-cnn.py), and 
got 100% accuracy on the test images. When the model is deployed in `PlaySet` it can play
a decent game of SET - see the animated GIF at the top of the page.

## References

* [BoofCV Java Computer Vision library](http://boofcv.org/index.php?title=Manual)
* [Vehicle Color Recognition using Convolutional Neural Network](https://arxiv.org/pdf/1510.07391.pdf)
