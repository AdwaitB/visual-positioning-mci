import csv
import os
import pickle
import cv2
import numpy as np

from config import *


def get_features_for_file(file_path):
    # Read the image
    img = cv2.imread(file_path)
    # Convert to grayscale
    print(file_path)
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    #sift
    sift = cv2.xfeatures2d.SIFT_create()
    # Detect the keypoints and descriptors
    keypoint, descriptor = sift.detectAndCompute(gray, None)
    print(descriptor.ndim)
    print(descriptor.shape)
    return keypoint, descriptor


# Function to get list of all folders inside a folder
def get_folders(folder):
    folders = []
    for subfolder in os.listdir(folder):
        if os.path.isdir(os.path.join(folder, subfolder)):
            folders.append(subfolder)
    return folders


# Function that takes a list of folders and reads the files inside them
def generate_features(folders):
    for folder in folders:
        # Get all the files in the folder
        files = os.listdir("./db/" + folder)
        # For each file, generate a feature vector
        for file in files:
            if not file.endswith(".jpeg") and not file.endswith(".jpg"):
                continue
            # Get the file path
            file_path = "./db/" + folder + "/" + file
            keypoint, descriptor = get_features_for_file(file_path)
            # Save the descriptor using numpy.save
            np.save("./db/" + folder + "/" + file + ".npy", descriptor)


def process_csv():
    with open(EDGES_PATH, 'r') as csv_file:
        csv_reader = csv.reader(csv_file)
        row_number = 0
        for row in csv_reader:
            if row_number == 0:
                row_number += 1
                continue
            folder = row[0]
            folder = "./db/"  + folder
            # Create the folder if it doesn't exist
            if not os.path.exists(folder):
                os.makedirs(folder)


def main():
    process_csv()
    tags = get_folders("./db/")
    generate_features(tags)


if __name__ == '__main__':
    main()