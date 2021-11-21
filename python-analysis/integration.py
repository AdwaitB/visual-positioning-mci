from scanner import *
import socket
import struct
import os
import pickle

import cv2
import numpy as np

from PIL import Image, ExifTags

IP_MOBILE = "0.0.0.0"
PORT = 8500

img_no = 0


if ORIGINAL_DATA:
    radius = 0.001
    # origin = [33.77435829374495, -84.397344643052] # Tech green
    # origin = [33.7754096889298, -84.39580062968061] # Bus Stop
    # origin = [33.77567129853454, -84.39715893627394] # Hive
    # origin = [33.77637681793485, -84.39690871277799] # Pettit
    # origin = [33.77692237952651, -84.39637874707857] # Klaus
    origin = [33.77570781753953, -84.39607218474292] # Industrial Design
    origin.reverse()

    DIFF = 0.001
else:
    radius = 6.5
    origin = [4, 4]

    DIFF = 0.1

direction = [-1, 1]

def calculateMatches(des1,des2):
    bf = cv2.BFMatcher()
    matches = bf.knnMatch(des1,des2,k=2)
    topResults1 = []
    for m,n in matches:
        if m.distance < 0.7*n.distance:
            topResults1.append([m])
            
    matches = bf.knnMatch(des2,des1,k=2)
    topResults2 = []
    for m,n in matches:
        if m.distance < 0.7*n.distance:
            topResults2.append([m])
    
    topResults = []
    for match1 in topResults1:
        match1QueryIndex = match1[0].queryIdx
        match1TrainIndex = match1[0].trainIdx

        for match2 in topResults2:
            match2QueryIndex = match2[0].queryIdx
            match2TrainIndex = match2[0].trainIdx

            if (match1QueryIndex == match2TrainIndex) and (match1TrainIndex == match2QueryIndex):
                topResults.append(match1)
    return topResults

def fetchKeypointFromFile(filepath):
    keypoint = []
    file = open(filepath,'rb')
    deserializedKeypoints = pickle.load(file)
    file.close()
    for point in deserializedKeypoints:
        temp = cv2.KeyPoint(x=point[0][0],y=point[0][1],size=point[1], angle=point[2], response=point[3], octave=point[4], class_id=point[5])
        keypoint.append(temp)
    return keypoint

def calculateScore(matches,keypoint1,keypoint2):
    return 100 * (matches/min(keypoint1,keypoint2))

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
    global img_no
    address = (IP_MOBILE, PORT)
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.bind(address)
    s.listen(1000)

    while True:
        conn, addr = s.accept()
        print('Connected by', addr)
        print('Receiving file')
        receive_file(conn, 'tst.jpg')
        img_no += 1
        img = Image.open("tst.jpg")
        exif = { ExifTags.TAGS[k]: v for k, v in img._getexif().items() if k in ExifTags.TAGS }
        conn.close()

        conn, addr = s.accept()
        print('Connected by', addr)
        print('Receiving degree')
        # Receive a float variable over the connection
        degree = conn.recv(1024)
        degree = hex_to_int(degree)
        print(degree)

        degree = 255

        conn.close()
        gpsInfo = exif['GPSInfo']

        origin = [-1 * dms_to_dd(gpsInfo[4]), dms_to_dd(gpsInfo[2])]
        direction = []

        # get cos and sin of the degree
        cos_d = math.cos(math.radians(degree))
        sin_d = math.sin(math.radians(degree))

        direction = [sin_d, cos_d]

        print(direction)

        edges = tag_edges_by_view(origin, direction, radius)

        print_util(edges, "edges final")

        black_edges = edges[(edges['color'] == 'black')]['tag'].tolist()
        print(black_edges)

        BASE_PATH = './db/'

        max_match = 0
        max_match_tag = None

        img = cv2.imread('./tst.jpg')

        gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

        sift = cv2.xfeatures2d.SIFT_create()

        querykeypoint, querydescriptor = sift.detectAndCompute(gray, None)

        

        for folder in black_edges:
            path = BASE_PATH + folder + '/'
            # get a list of all jpg files in path folder
            files = [f for f in os.listdir(path) if f.endswith('.jpeg') or f.endswith('.jpg')]
            print(files)
            for file in files:
                # bf = cv2.BFMatcher(cv2.NORM_L1, crossCheck=True)
                bf = cv2.BFMatcher()

                keypoint_file_path = path + file + ".pkl"
                descriptor_file_path = path + file + ".npy"

                comparekeypoint = fetchKeypointFromFile(keypoint_file_path)
                comparedescriptor = np.load(descriptor_file_path)

                # matches = bf.match(querydescriptor, comparedescriptor)
                # matches = sorted(matches, key = lambda x:x.distance)
                # score = calculateScore(len(matches), len(querykeypoint), len(comparekeypoint))

                matches = calculateMatches(querydescriptor, comparedescriptor)
                score = calculateScore(len(matches), len(querykeypoint), len(comparekeypoint))

                print(score)
                if score > max_match:
                    max_match = score
                    max_match_tag = folder
                print(max_match_tag)




                



        print('Reached end of loop')


if __name__ == '__main__':
    main()