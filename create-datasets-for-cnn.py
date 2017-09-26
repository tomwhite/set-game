import os
import random
import shutil

# We need to change the dataset so that it is split into train/validation/test
# portions, and labelled with a single attribute (e.g. 'color').

attributes = ('color', 'number', 'shape', 'shading', 'all')

attribute_label_extraction_fns = {
    'number': lambda dir: dir.split('-')[0],
    'color': lambda dir: dir.split('-')[1],
    'shading': lambda dir: dir.split('-')[2],
    'shape': lambda dir: dir.split('-')[3].rstrip('s'), # remove trailing 's'
    'all': lambda dir: dir
}

def copyfile(src_dir, dest_dir, file):
    if not os.path.exists(dest_dir):
        os.makedirs(dest_dir)
    shutil.copyfile(os.path.join(src_dir, file), os.path.join(dest_dir, file))

def create_split_datasets(dataset_dir, target_dir, label_extract_fn,
                          train_split_percent, validation_split_percent, test_split_percentage):

    dirs = []
    for (dirpath, dirnames, filenames) in os.walk(dataset_dir):
        dirs.extend(dirnames)
        break

    target_train_dir = os.path.join(target_dir, 'train')
    target_validation_dir = os.path.join(target_dir, 'validation')
    target_test_dir = os.path.join(target_dir, 'test')

    for dir in dirs:
        subdir = os.path.join(dataset_dir, dir)
        files = os.listdir(subdir)
        random.shuffle(files)
        i1 = int(len(files) * train_split_percent / 100)
        i2 = int(len(files) * (train_split_percent + validation_split_percent) / 100)
        train, validation, test = files[:i1], files[i1:i2], files[i2:]
        label = label_extract_fn(dir)

        for file in train:
            copyfile(subdir, os.path.join(target_train_dir, label), file)
        for file in validation:
            copyfile(subdir, os.path.join(target_validation_dir, label), file)
        for file in test:
            copyfile(subdir, os.path.join(target_test_dir, label), file)

def create_single_attribute_test_dataset(dataset_dir, target_dir, label_extract_fn):
    dirs = []
    for (dirpath, dirnames, filenames) in os.walk(dataset_dir):
        dirs.extend(dirnames)
        break

    for dir in dirs:
        files = os.listdir(os.path.join(dataset_dir, dir))
        label = label_extract_fn(dir)
        for file in files:
            copyfile(os.path.join(dataset_dir, dir), os.path.join(target_dir, label),
                     file)

for attribute in attributes:
    create_split_datasets('data/train-v2/labelled', f'data/{attribute}',
                          attribute_label_extraction_fns[attribute],
                          70, 20, 10)
    create_single_attribute_test_dataset('data/test-v2', f'data/{attribute}-test',
                          attribute_label_extraction_fns[attribute])
    
# Create an artificially small training dataset to observe overfitting
create_split_datasets('data/train-v2/labelled', f'data/shape-small',
                          attribute_label_extraction_fns['shape'],
                          1, 20, 79)