import numpy as np

from config import *
from euclidean import *
from building import *


def tag_points_fov(points):
    colors = [x for x in points['color']]
    normalized_angles = []

    reference_angle = get_angle(DIRECTION)
    semicircle_angle_start = reference_angle - 90
    semicircle_angle_end = reference_angle + 90
    fov_angle_start = reference_angle - FOV / 2
    fov_angle_end = reference_angle + FOV / 2

    if DEBUG_LEVEL >= 1:
        print("reference_angle : ", reference_angle)
        print("semicircle_angle_start : ", semicircle_angle_start)
        print("semicircle_angle_end: ", semicircle_angle_end)
        print("fov_angle_start: ", fov_angle_start)
        print("fov_angle_end: ", fov_angle_end)

    for index, row in points.iterrows():
        angle = get_angle([row['x'] - ORIGIN[0], row['y'] - ORIGIN[1]])
        normalized_angles.append(normalize_angle(semicircle_angle_start, angle))

        dist = get_distance_points(ORIGIN, [row['x'], row['y']])

        if dist > RADIUS:
            continue

        if check_in_angle_range(fov_angle_start, fov_angle_end, angle):
            colors[index] = POINT_VIEW
        elif check_in_angle_range(semicircle_angle_start, fov_angle_start, angle):
            colors[index] = POINT_SEMI_1
        elif check_in_angle_range(fov_angle_end, semicircle_angle_end, angle):
            colors[index] = POINT_SEMI_2

    points['color'] = colors
    points['normalized_angle'] = normalized_angles


def tag_edges_fov(points, edges):
    colors = [x for x in edges['color']]

    for i, edge in edges.iterrows():
        if (points['color'][edge['start']] == POINT_SEMI_1) and (
                points['color'][edge['end']] == POINT_SEMI_2):
            colors[i] = EDGE_SPLIT
        elif (points['color'][edge['start']] == POINT_SEMI_2) and (
                points['color'][edge['end']] == POINT_SEMI_1):
            colors[i] = EDGE_SPLIT
        elif (points['color'][edge['start']] == POINT_VIEW) or (
                points['color'][edge['end']] == POINT_VIEW):
            colors[i] = EDGE_VIEW

    edges['color'] = colors


def tag_boundary_edges(points, edges):
    colors = [x for x in edges['color']]

    # Can be optimized to linear time using topo sort
    for i, ei in edges.iterrows():
        # Check if edge is in the scan or not
        if ei['color'] == EDGE_DEFAULT:
            continue

        # Check if this edge is not overlappable
        for j, ej in edges.iterrows():
            if (i == j) or (ej['color'] == EDGE_DEFAULT):
                continue

            if check_edge_overlap(edges, points, j, i, ORIGIN):
                colors[i] = EDGE_DEFAULT

                if DEBUG_LEVEL >= 1:
                    print(j, " overlaps ", i)

                break

    edges['color'] = colors


def tag_feasible_edges(points, edges, edges_list, buildings, id_to_build_map):
    points_temp = []
    building_ids = set()

    colors = [x for x in edges['color']]

    for i, point in points.iterrows():
        if point['color'] != POINT_VIEW:
            continue

        points_temp.append([
            get_distance_points(ORIGIN, [point['x'], point['y']]),
            point['normalized_angle'], i, point['x'], point['y']
        ])

        building_ids.add(id_to_build_map[i])

    if DEBUG_LEVEL >= 1:
        print(building_ids)
    points_temp.sort()

    for building_id in building_ids:
        valid, invalid = split_edges_building(points, edges_list, buildings, building_id)

        for edge_id in invalid:
            colors[edge_id] = EDGE_DEFAULT

    edges['color'] = colors
    return

