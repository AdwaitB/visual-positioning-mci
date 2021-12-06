from scanner import *
from matcher import *
import socket
import struct
import os
import pickle

import cv2
import numpy as np

from PIL import Image, ExifTags

from preprocessor import get_features

img_no = 0
radius = 0.001


def fetchKeypointFromFile(filepath):
    keypoint = []
    file = open(filepath,'rb')
    deserializedKeypoints = pickle.load(file)
    file.close()
    for point in deserializedKeypoints:
        temp = cv2.KeyPoint(x=point[0][0], y=point[0][1], size=point[1], angle=point[2],
                            response=point[3], octave=point[4], class_id=point[5])
        keypoint.append(temp)
    return keypoint


def calculate_score(matches, keypoint1, keypoint2):
    return 100 * (len(matches)/min(len(keypoint1), len(keypoint2)))


# Function to convert hex byte array to int
def hex_to_int(hex_array):
    return int(hex_array.hex(), 16)


def receive_file(conn, file_name):
    buf = bytes()
    while len(buf)<4:
        buf += conn.recv(4-len(buf))
    size = struct.unpack('!i', buf)

    # Receive the file
    with open(file_name, 'wb') as f:
        while True:
            data = conn.recv(1024)
            if not data:
                break
            f.write(data)

    print('File received successfully')


# Convert degrees minutes second to decimal degrees
def dms_to_dd(dms):
    degrees = dms[0]
    minutes = dms[1]
    seconds = dms[2]

    dd = degrees + (minutes/60) + (seconds/3600)
    return dd

data = {
    "1.jpg": [0,1],
    "2.jpg": [1,0],
    "3.jpg": [0,1],
    "4.jpg": [-1,1],
    "5.jpg": [-2,1],
    "6.jpg": [1,1],
    "7.jpg": [-1,2],
    "8.jpg": [1,1],
    "9.jpg": [0,1],
    "10.jpg": [-1,-1],
    "11.jpg": [1,2],
    "12.jpg": [-10,-1],
}

def main():
    file_name = '7.jpg'
    img = Image.open(INTEGRATION_LOCAL_DIRECTORY + '/' + file_name)
    exif = { ExifTags.TAGS[k]: v for k, v in img._getexif().items() if k in ExifTags.TAGS }

    gpsInfo = exif['GPSInfo']
    print(gpsInfo)

    origin = [-1 * dms_to_dd(gpsInfo[4]), dms_to_dd(gpsInfo[2])]
    direction = data[file_name]
    print(direction)

    edges = tag_edges_by_view(origin, direction, radius)

    print("Edges are:" + str(edges))

    black_edges = edges[(edges['color'] == 'black')]['tag'].tolist()
    print("Black edges are:" + str(black_edges))

    matches_above_threshold = []

    img = cv2.imread(INTEGRATION_LOCAL_DIRECTORY + '/' + file_name)

    querykeypoint, querydescriptor, processed_img = get_features(img)

    for folder in black_edges:
        path = DB_PATH + folder + '/'
        # get a list of all jpg files in path folder
        files = [f for f in os.listdir(path) if f.endswith('.jpeg') or f.endswith('.jpg')]
        print("Files:" + str(files))
        for file in files:
            keypoint_file_path = path + file + ".pkl"
            descriptor_file_path = path + file + ".npy"

            comparekeypoint = fetchKeypointFromFile(keypoint_file_path)
            comparedescriptor = np.load(descriptor_file_path)

            matches = match_feature_points(querydescriptor, comparedescriptor)
            score = len(lowes_ratio_test(matches))

            print("File being compared is " + str(file))
            print(score)

            if score >= MATCHING_THRESHOLD:
                matches_above_threshold.append([folder, file])


    for [folder, file] in matches_above_threshold:
        print("Folder: " + str(folder) + " File: " + str(file))
        im = Image.open(DB_PATH + folder + '/' + file)
        im.show()


if __name__ == '__main__':
    main()
