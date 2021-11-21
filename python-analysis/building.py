import numpy as np

from misc_utils import *
from euclidean import *
from config import *


def get_building_set(points, edges_list):
    id_to_build_map = {}
    buildings = {}
    build_id = 0

    visited = set()

    for index, point in points.iterrows():
        if index in visited or point['color'] == POINT_DEFAULT:
            continue

        if DEBUG_LEVEL >= 1:
            print("in ", index)

        scope = []

        frontier = [index]
        scope.append(index)
        visited.add(index)

        while len(frontier) != 0:
            top_id = frontier.pop()

            for [next_point, edge_id] in edges_list[top_id]:
                if next_point in visited:
                    continue

                frontier.append(next_point)
                visited.add(next_point)
                scope.append(next_point)

        if DEBUG_LEVEL >= 1:
            print("total ", scope)

        for entry in scope:
            id_to_build_map[entry] = len(buildings)

        buildings[build_id] = scope
        build_id = build_id + 1

    return id_to_build_map, buildings


def split_edges_building(origin, points, edges, edges_list, building):
    colors = [x for x in edges['color']]

    invalid_edges = set()
    valid_edges = set()

    points_sorted = []

    for point_id in building:
        point = points.iloc[point_id]

        if not np.isnan(point['normalized_angle']):
            points_sorted.append([
                get_distance_points(origin, [point['x'], point['y']]),
                point['normalized_angle'], point_id, point['x'], point['y']
            ])

    points_sorted.sort()

    if len(points_sorted) == 0:
        return invalid_edges, valid_edges

    if DEBUG_LEVEL >= 0:
        print_util(points_sorted)

    if len(points_sorted) == 0:
        return valid_edges, invalid_edges

    angle_start = points_sorted[0][1]
    angle_end = points_sorted[0][1]

    for point in points_sorted:
        if DEBUG_LEVEL >= 1:
            print("in ", point)

        for [other_node, edge_id] in edges_list[point[2]]:
            if edge_id in valid_edges or edge_id in invalid_edges:
                continue

            if DEBUG_LEVEL >= 1:
                print(other_node, " ", edge_id, " ", angle_start, " ", angle_end)

            changed = False

            if points['normalized_angle'][other_node] < angle_start:
                changed = True
                angle_start = points['normalized_angle'][other_node]

            if points['normalized_angle'][other_node] > angle_end:
                changed = True
                angle_end = points['normalized_angle'][other_node]

            if angle_start > point[1]:
                changed = True
                angle_start = point[1]

            if angle_end < point[1]:
                changed = True
                angle_end = point[1]

            if changed:
                valid_edges.add(edge_id)
            else:
                invalid_edges.add(edge_id)
                colors[edge_id] = EDGE_DEFAULT

            if DEBUG_LEVEL >= 1:
                print(other_node, " ", edge_id, " ", angle_start, " ", angle_end)

    edges['color'] = colors
    return list(valid_edges), list(invalid_edges), points_sorted[0][2]


def collect_span(span_edges, start_point, edges, points):
    if len(span_edges) == 0:
        return [start_point, start_point]

    span_start_pt = edges['start'][span_edges[0]]
    span_end_pt = edges['start'][span_edges[0]]

    span_start = points['normalized_angle'][edges['start'][span_edges[0]]]
    span_end = points['normalized_angle'][edges['start'][span_edges[0]]]

    for span_edge in span_edges:
        angle_1 = points['normalized_angle'][edges['start'][span_edge]]
        angle_2 = points['normalized_angle'][edges['end'][span_edge]]

        if span_start > angle_1:
            span_start = angle_1
            span_start_pt = edges['start'][span_edge]
        if span_start > angle_2:
            span_start = angle_2
            span_start_pt = edges['end'][span_edge]
        if span_end < angle_1:
            span_end = angle_1
            span_end_pt = edges['start'][span_edge]
        if span_end < angle_2:
            span_end = angle_2
            span_end_pt = edges['end'][span_edge]

    return [span_start_pt, span_end_pt]


def get_spans(origin, points, edges, edges_list, buildings):
    ret = {}

    for i in buildings.keys():
        valid, invalid, start_point = split_edges_building(origin, points, edges, edges_list, buildings[i])

        if DEBUG_LEVEL >= 1:
            print_util(i, "building ")
            print_util(valid, "valid")
            print_util(invalid, "invalid")

        span = collect_span(valid, start_point, edges, points)

        if DEBUG_LEVEL >= 1:
            print_util(span, "span")

        ret[i] = span

    return ret
