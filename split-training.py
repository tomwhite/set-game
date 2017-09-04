import os
import random
import shutil

train_split_percent, validation_split_percent, test_split_percentage = 70, 20, 10

dataset_dir = '/Users/tom/projects-workspace/set-game/data/train-v2/labelled'
target_dir = '/Users/tom/tmp/set'
dirs = []
for (dirpath, dirnames, filenames) in os.walk(dataset_dir):
    dirs.extend(dirnames)
    break

os.mkdir(target_dir)
target_train_dir = os.path.join(target_dir, 'train')
target_validation_dir = os.path.join(target_dir, 'validation')
target_test_dir = os.path.join(target_dir, 'test')
os.mkdir(target_train_dir)
os.mkdir(target_validation_dir)
os.mkdir(target_test_dir)

colours = ('green', 'purple', 'red')

for label in colours:
  os.mkdir(os.path.join(target_dir, 'train', label))
  os.mkdir(os.path.join(target_dir, 'validation', label))
  os.mkdir(os.path.join(target_dir, 'test', label))

for dir in dirs:
  files = os.listdir(os.path.join(dataset_dir, dir))
  random.shuffle(files)
  i1 = int(len(files) * train_split_percent / 100)
  i2 = int(len(files) * (train_split_percent + validation_split_percent) / 100)
  train, validation, test = files[:i1], files[i1:i2], files[i2:]
  label = dir.split('-')[1] # colour
  
  for file in train:
    shutil.copyfile(os.path.join(dataset_dir, dir, file), os.path.join(target_train_dir, label, file))
  for file in validation:
    shutil.copyfile(os.path.join(dataset_dir, dir, file), os.path.join(target_validation_dir, label, file))
  for file in test:
    shutil.copyfile(os.path.join(dataset_dir, dir, file), os.path.join(target_test_dir, label, file))
