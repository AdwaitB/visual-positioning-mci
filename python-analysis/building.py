from euclidean import *
from config import *


def tag_points(points):
    colors = [DATA_POINT_COLOR_DEFAULT] * len(points)
    normalized_angles = []

    reference_angle = get_angle(DIRECTION)
    semicircle_angle_start = reference_angle - 90
    semicircle_angle_end = reference_angle + 90
    fov_angle_start = reference_angle - FOV / 2
    fov_angle_end = reference_angle + FOV / 2

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
            colors[index] = DATA_POINT_COLOR_VIEW
        elif check_in_angle_range(semicircle_angle_start, fov_angle_start, angle):
            colors[index] = DATA_POINT_COLOR_SEMI_1
        elif check_in_angle_range(fov_angle_end, semicircle_angle_end, angle):
            colors[index] = DATA_POINT_COLOR_SEMI_2

    points['color'] = colors
    points['normalized_angle'] = normalized_angles


def get_building_set(points, edge_ids_list):
    id_to_build_map = {}
    buildings = []

    visited = set()

    for index, point in points.iterrows():
        if index in visited:
            continue

        print("in ", index)

        scope = []

        frontier = [index]
        scope.append(index)
        visited.add(index)

        while len(frontier) != 0:
            top_id = frontier.pop()

            for [next_point, edge_id] in edge_ids_list[top_id]:
                if next_point in visited:
                    continue

                frontier.append(next_point)
                visited.add(next_point)
                scope.append(next_point)

        print("total ", scope)

        for entry in scope:
            id_to_build_map[entry] = len(buildings)

        buildings.append(scope)

    return id_to_build_map, buildings


def split_edges_building(points, edge_ids_list, buildings, building_id):
    invalid_edges = set()
    valid_edges = set()

    points_temp = []

    for point_id in buildings[building_id]:
        point = points.iloc[point_id]

        if not np.isnan(point['normalized_angle']):
            points_temp.append([
                get_distance_points(ORIGIN, [point['x'], point['y']]),
                point['normalized_angle'], point_id, point['x'], point['y']
            ])

    points_temp.sort()

    pprint(points_temp)

    if len(points_temp) == 0:
        return valid_edges, invalid_edges

    angle_start = points_temp[0][1]
    angle_end = points_temp[0][1]

    for point in points_temp:
        print("in ", point)
        for [other_node, edge_id] in edge_ids_list[point[2]]:
            if edge_id in valid_edges or edge_id in invalid_edges:
                continue

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

            print(other_node, " ", edge_id, " ", angle_start, " ", angle_end)
    return valid_edges, invalid_edges



