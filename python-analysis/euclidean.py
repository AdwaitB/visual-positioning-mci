import math

import random as rd
import numpy as np
import pandas as pd

from pprint import pprint


def get_angle(point):
    x = point[0]
    y = point[1]
    size = math.sqrt(x * x + y * y)
    angle = math.acos(x / size) * 180 / math.pi

    if y < 0:
        angle = 360 - angle

    return angle


def normalize_vector(point):
    x = point[0]
    y = point[1]
    size = math.sqrt(x*x + y*y)
    return [x/size, y/size]


def rotate_point(point, base, angle):
    s = math.sin(angle * math.pi / 180)
    c = math.cos(angle * math.pi / 180)

    x = point[0] - base[0]
    y = point[1] - base[1]

    return [
        x * c - y * s + base[0],
        x * s + y * c + base[1]
    ]


def add_vector_to_point(point, vector, size):
    vector = normalize_vector(vector)
    return [point[0] + vector[0]*size, point[1] + vector[1]*size]


def normalize_angle(reference, angle):
    ret = angle - reference
    if ret < 0:
        ret += 360
    return ret


def check_in_angle_range(start, end, angle):
    if (angle >= start) and (angle <= end):
        return True

    angle = angle - 360

    if (angle >= start) and (angle <= end):
        return True

    angle = angle + 720

    if (angle >= start) and (angle <= end):
        return True

    return False


def get_line(p1, p2):
    m = (p2[1] - p1[1])/(p2[0] - p1[0])
    return [m, -1, p1[1] - m*p1[0]]


def get_perp_distance(line, point):
    num = line[0] * point[0] + line[1] * point[1] + line[3]
    if num < -1:
        num = num * (-1)

    den = math.sqrt(line[0] * line[0] + line[1] * line[1])

    return num / den


def get_distance_points(p1, p2):
    dx = p1[0] - p2[0]
    dy = p1[1] - p2[1]

    return math.sqrt(dx * dx + dy * dy)


def get_perp_distance_points(p1, p2, point):
    return get_perp_distance(get_line(p1, p2), point)


def check_edge_overlap(edges, points, e1, e2, origin):
    # First check angle subset
    angle_1 = [points['normalized_angle'][edges.iloc[e1]['start']],
               points['normalized_angle'][edges.iloc[e1]['end']]]

    angle_2 = [points['normalized_angle'][edges.iloc[e2]['start']],
               points['normalized_angle'][edges.iloc[e2]['end']]]

    angle_1.sort()
    angle_2.sort()

    angle_overlap = (angle_1[0] <= angle_2[0]) and (angle_1[1] >= angle_2[1])

    print()
    print(angle_1, " ", angle_2, " ", angle_overlap)

    # Now check distance min
    min_1 = min(
        get_distance_points(
            [
                points['x'][edges.iloc[e1]['start']],
                points['y'][edges.iloc[e1]['start']]
            ], origin),
        get_distance_points(
            [
                points['x'][edges.iloc[e1]['end']],
                points['y'][edges.iloc[e1]['end']]
            ], origin)
    )

    min_2 = min(
        get_distance_points(
            [
                points['x'][edges.iloc[e2]['start']],
                points['y'][edges.iloc[e2]['start']]
            ], origin),
        get_distance_points(
            [
                points['x'][edges.iloc[e2]['end']],
                points['y'][edges.iloc[e2]['end']]
            ], origin)
    )

    distance_check = min_1 <= min_2

    print(min_1, " ", min_2, " ", distance_check)

    return angle_overlap and distance_check
