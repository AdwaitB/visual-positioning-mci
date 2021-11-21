import numpy as np

from plot import *
from data_generator import *
from db_utils import *
from building import *


def tag_points_fov(origin, direction, radius, points):
    colors = [x for x in points['color']]
    normalized_angles = []

    reference_angle = get_angle(direction)
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
        angle = get_angle([row['x'] - origin[0], row['y'] - origin[1]])
        normalized_angles.append(normalize_angle(semicircle_angle_start, angle))

        dist = get_distance_points(origin, [row['x'], row['y']])

        if dist > radius:
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


def remove_overshadowing_edges(origin, points, edges):
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

            if check_edge_overlap(origin, points, edges['start'][i], edges['end'][i], edges['start'][j], edges['end'][j]):
                colors[i] = EDGE_DEFAULT

                if DEBUG_LEVEL >= 1:
                    print(j, " overlaps ", i)

                break

    edges['color'] = colors


def remove_overshadowing_edges_spans(origin, points, edges, spans):
    colors = [x for x in edges['color']]

    # Can be optimized to linear time using topo sort
    for i in spans:
        span_points = spans[i]

        # Check if this edge is not overlappable
        for j, ej in edges.iterrows():
            if ej['color'] == EDGE_DEFAULT:
                continue

            if check_edge_overlap(origin, points, span_points[0], span_points[1], edges['start'][j], edges['end'][j]):
                colors[j] = EDGE_DEFAULT

                if DEBUG_LEVEL >= 1:
                    print(j, " overlaps ", j)

                break

    edges['color'] = colors


def tag_edges_by_view(origin, direction, radius):
    if ORIGINAL_DATA:
        points = read_points()
        edges = read_edges(points)
    else:
        points = generate_data(NPOINTS)
        edges = get_edges()

    if DEBUG_LEVEL >= 0:
        print_util(points, "points")
        print_util(edges, "edges")

    edges_mat = get_adj_mat(points, edges)
    if DEBUG_LEVEL >= 0:
        print_util(edges_mat, "edges_mat")

    edges_list = get_adj_list(points, edges)
    if DEBUG_LEVEL >= 0:
        print_util(edges_list, "edges_list")

    ###################

    base_angle = get_angle(direction)

    viewpoint = add_vector_to_point(origin, direction, radius)

    viewpoint_begin = rotate_point(viewpoint, origin, FOV / 2)
    viewpoint_end = rotate_point(viewpoint, origin, -FOV / 2)

    axis_xp = rotate_point(viewpoint, origin, -90)
    axis_xn = rotate_point(viewpoint, origin, 90)

    if DEBUG_LEVEL >= 0:
        print("base_angle ", base_angle)
        print("viewpoint ", viewpoint)
        print("viewpoint_begin ", viewpoint_begin)
        print("viewpoint_end ", viewpoint_end)
        print("axis_xp ", axis_xp)
        print("axis_xn ", axis_xn)

    ###################

    tag_points_fov(origin, direction, radius, points)
    if DEBUG_LEVEL >= 0:
        print_util(points, "points")

    ###################

    tag_edges_fov(points, edges)
    if DEBUG_LEVEL >= 0:
        print_util(edges)

    ###################

    id_to_build_map, buildings = get_building_set(points, edges_list)
    if DEBUG_LEVEL >= 0:
        print_util(id_to_build_map, "id_to_build_map")
        print_util(buildings, "buildings")

    ###################

    spans = get_spans(origin, points, edges, edges_list, buildings)
    if DEBUG_LEVEL >= 0:
        print_util(spans, "spans")

    ###################

    remove_overshadowing_edges(origin, points, edges)
    if DEBUG_LEVEL >= 0:
        print_util(edges, "edges after overshadow filter")

    ###################

    remove_overshadowing_edges_spans(origin, points, edges, spans)
    if DEBUG_LEVEL >= 0:
        print_util(edges, "edges after spanned overshadow filter")

    ###################

    plot_situation(origin, radius, points, edges, viewpoint, viewpoint_begin, viewpoint_end, axis_xp, axis_xn)

    return edges

