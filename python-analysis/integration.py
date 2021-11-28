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

IP_MOBILE = "0.0.0.0"
PORT = 8500

img_no = 0


if ORIGINAL_DATA:
    radius = 0.001

    data = {
        "TG": [[33.77435829374495, -84.397344643052], [1, 0]],
        "BusStop": [[33.7754096889298, -84.39580062968061], [0, -1]],
        "Hive1": [[33.77567129853454, -84.39715893627394], [1, 1]],
        "Hive2": [[33.77567129853454, -84.39715893627394], [-1, -1]],
        "Pettit": [[33.77637681793485, -84.39690871277799], [1, 1]],
        "Klaus1": [[33.77692237952651, -84.39637874707857], [1, 1]],
        "Klaus2": [[33.77692237952651, -84.39637874707857], [-1, 1]],
        "IndustrialDesign": [[33.77570781753953, -84.39607218474292], [1, 1]],
        "CULC": [[33.773985, -84.396579], [0, 1]]
    }
    origin, direction = data["CULC"]
    origin.reverse()

    DIFF = 0.001
else:
    radius = 6.5
    origin = [4, 4]

    DIFF = 0.1

direction = [-1, 1]


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


def main():
    if not INTEGRATION_LOCAL:
        global img_no
        address = (IP_MOBILE, PORT)
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.bind(address)
        s.listen(1000)

    while True:
        if not INTEGRATION_LOCAL:
            conn, addr = s.accept()
            print('Connected by', addr)
            print('Receiving file')
            receive_file(conn, INTEGRATION_COMM_FILE)
            img_no += 1
            img = Image.open(INTEGRATION_COMM_FILE)
            exif = { ExifTags.TAGS[k]: v for k, v in img._getexif().items() if k in ExifTags.TAGS }
            conn.close()

            conn, addr = s.accept()
            print('Connected by', addr)
            print('Receiving degree')
            # Receive a float variable over the connection
            degree = conn.recv(1024)
            degree = hex_to_int(degree)
            print(degree)

            conn.close()
            gpsInfo = exif['GPSInfo']

            origin = [-1 * dms_to_dd(gpsInfo[4]), dms_to_dd(gpsInfo[2])]
            direction = []

            # get cos and sin of the degree
            cos_d = math.cos(math.radians(degree))
            sin_d = math.sin(math.radians(degree))

            direction = [sin_d, cos_d]
            print(direction)
        else:
            origin, direction = data["CULC"]

        img = cv2.imread(INTEGRATION_LOCAL_FILE)
        edges = tag_edges_by_view(origin, direction, radius)

        print("Edges are:" + str(edges))

        black_edges = edges[(edges['color'] == 'black')]['tag'].tolist()
        print("Black edges are:" + str(black_edges))

        max_match = 0
        max_match_tag = None
        max_file = None

        querykeypoint, querydescriptor, processed_img = get_features(img)

        for folder in black_edges:
            path = DB_PATH + folder + '/'
            # get a list of all jpg files in path folder
            files = [f for f in os.listdir(path) if f.endswith('.jpeg') or f.endswith('.jpg')]
            print(files)
            for file in files:
                keypoint_file_path = path + file + ".pkl"
                descriptor_file_path = path + file + ".npy"

                comparekeypoint = fetchKeypointFromFile(keypoint_file_path)
                comparedescriptor = np.load(descriptor_file_path)

                matches = match_feature_points(querydescriptor, comparedescriptor)
                score = len(lowes_ratio_test(matches))

                print(file)
                print(score)

                if score > max_match:
                    max_match = score
                    max_match_tag = folder
                    max_file = file

        print(max_match)
        print("Probable location is :")
        print(max_match_tag)
        print('Reached end of loop')

        # Use cv2 to read an image and display it
        max_match_file = DB_PATH + max_match_tag + '/' + max_file
        print(max_match_file)

        im = Image.open(max_match_file)
        im.show()


        if INTEGRATION_LOCAL:
            break


if __name__ == '__main__':
    main()
