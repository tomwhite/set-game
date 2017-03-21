The _train-v2_ dataset is a collection of Set card images.

The _raw-archive_ directory contains original camera images. Each image is a 
photo of a board of 27 images, arranged in a 3 by 9 grid. There are three
boards, one contains all the Set cards with one shape, one with two shapes,
and one with three shapes.

The _raw-archive_ images are photos of the same boards taken at different times under
varying lighting conditions, from different angles, and with different cameras.

### Process

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

