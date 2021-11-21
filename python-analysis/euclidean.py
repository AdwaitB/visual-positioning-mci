import math

from config import *


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
    if ret < -180:
        ret += 360
    if ret > 180:
        ret -= 360
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


def check_edge_overlap(origin, points, p11, p12, p21, p22):
    if (p11 == p21) and (p12 == p22):
        return False
    elif (p11 == p22) and (p12 == p21):
        return False

    # First check angle subset
    angle_1 = [points['normalized_angle'][p11],
               points['normalized_angle'][p12]]

    angle_2 = [points['normalized_angle'][p21],
               points['normalized_angle'][p22]]

    angle_1.sort()
    angle_2.sort()

    angle_overlap = (angle_1[0] <= angle_2[0]) and (angle_1[1] >= angle_2[1])

    if DEBUG_LEVEL >= 1:
        print(angle_1, " ", angle_2, " ", angle_overlap)

    # Now check distance min
    min_1 = min(
        get_distance_points(
            [
                points['x'][p11],
                points['y'][p11]
            ], origin),
        get_distance_points(
            [
                points['x'][p12],
                points['y'][p12]
            ], origin)
    )

    min_2 = min(
        get_distance_points(
            [
                points['x'][p21],
                points['y'][p21]
            ], origin),
        get_distance_points(
            [
                points['x'][p22],
                points['y'][p22]
            ], origin)
    )

    distance_check = min_1 <= min_2

    if DEBUG_LEVEL >= 1:
        print(min_1, " ", min_2, " ", distance_check)

    return angle_overlap and distance_check
