from config import *
from euclidean import *
from building import *


def tag_feasible_edges(points, edge_ids, edge_ids_list, buildings, id_to_build_map):
    points_temp = []
    building_ids = set()

    colors = [x for x in edge_ids['color']]

    for i, point in points.iterrows():
        if point['color'] != DATA_POINT_COLOR_VIEW:
            continue

        points_temp.append([
            get_distance_points(ORIGIN, [point['x'], point['y']]),
            point['normalized_angle'], i, point['x'], point['y']
        ])

        building_ids.add(id_to_build_map[i])

    print(building_ids)
    points_temp.sort()

    for building_id in building_ids:
        valid, invalid = split_edges_building(points, edge_ids_list, buildings, building_id)

        for edge_id in invalid:
            colors[edge_id] = DATA_EDGE_COLOR_DEFAULT

    edge_ids['color'] = colors
    return


def tag_valid_edges(points, edge_ids):
    colors = [x for x in edge_ids['color']]

    for i, edge in edge_ids.iterrows():
        if (points['color'][edge['start']] == DATA_POINT_COLOR_SEMI_1) and (
                points['color'][edge['end']] == DATA_POINT_COLOR_SEMI_2):
            colors[i] = DATA_EDGE_COLOR_SEMI
        elif (points['color'][edge['start']] == DATA_POINT_COLOR_SEMI_2) and (
                points['color'][edge['end']] == DATA_POINT_COLOR_SEMI_1):
            colors[i] = DATA_EDGE_COLOR_SEMI
        elif (points['color'][edge['start']] == DATA_POINT_COLOR_VIEW) or (
                points['color'][edge['end']] == DATA_POINT_COLOR_VIEW):
            colors[i] = DATA_EDGE_COLOR_VIEW

    edge_ids['color'] = colors


def tag_boundary_edges(points, edge_ids):
    colors = [x for x in edge_ids['color']]

    # Can be optimized to linear time using topo sort
    for i, ei in edge_ids.iterrows():
        # Check if edge is in the scan or not
        if ei['color'] == DATA_EDGE_COLOR_DEFAULT:
            continue

        # Check if this edge is not overlappable
        for j, ej in edge_ids.iterrows():
            if (i == j) or (ej['color'] == DATA_EDGE_COLOR_DEFAULT):
                continue

            if check_edge_overlap(edge_ids, points, j, i, ORIGIN):
                colors[i] = DATA_EDGE_COLOR_DEFAULT
                print(j, " overlaps ", i)
                break

    edge_ids['color'] = colors

