import csv
import os
import pickle
import cv2
import time

import numpy as np

from config import *


def resize(img, scale=30):
    w = int(img.shape[1]*scale/100)
    h = int(img.shape[0]*scale/100)
    resized = cv2.resize(img, (w, h), interpolation=cv2.INTER_AREA)
    return resized


def get_features(img):
    start = time.time()
    temp = resize(img, 40)

    temp = cv2.fastNlMeansDenoisingColored(temp, None, 10, 10, 7, 21)
    temp = cv2.cvtColor(temp, cv2.COLOR_BGR2GRAY)
    temp = cv2.GaussianBlur(temp, (3, 3), 0)
    temp = cv2.Canny(image=temp, threshold1=100, threshold2=200)
    thresh, temp = cv2.threshold(temp, 127, 255, cv2.THRESH_BINARY)

    if FEATURE == "SIFT":
        sift = cv2.SIFT_create()
        keypoints, descriptor = sift.detectAndCompute(temp, None)
    elif FEATURE == "ORB":
        orb = cv2.ORB_create(nfeatures=2000)
        keypoints, descriptor = orb.detectAndCompute(temp, None)

    temp = cv2.drawKeypoints(temp, keypoints, img, flags=cv2.DRAW_MATCHES_FLAGS_DRAW_RICH_KEYPOINTS)

    end = time.time()
    print("Features took ", end - start, " seconds.")
    return keypoints, descriptor, temp


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
        print()
        print(folder)
        # Get all the files in the folder
        files = os.listdir("./db/" + folder)
        # For each file, generate a feature vector
        for file in files:
            if not file.endswith(".jpeg") and not file.endswith(".jpg"):
                continue
            # Get the file path
            file_path = "./db/" + folder + "/" + file
            print(file_path)
            img = cv2.imread(file_path)
            keypoint, descriptor, processed_img = get_features(img)
            # Save the descriptor using numpy.save
            np.save("./db/" + folder + "/" + file + ".npy", descriptor)

            deserialized_keypoints = []
            for point in keypoint:
                temp = (point.pt, point.size, point.angle, point.response, point.octave, point.class_id)
                deserialized_keypoints.append(temp)
            with open("./db/" + folder + "/" + file + ".pkl", 'wb') as f:
                pickle.dump(deserialized_keypoints, f)


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