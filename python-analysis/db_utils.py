import pandas as pd

from config import *


def read_points(filename="points.csv"):
    data = pd.read_csv(filename)
    data['color'] = [POINT_DEFAULT]*len(data)
    return data


def read_edges(points, filename="edges.csv"):
    data = pd.read_csv(filename)
    data['color'] = [EDGE_DEFAULT]*len(data)

    print(data['p1_tag'])

    point_tag_to_id = {}

    for index, point in points.iterrows():
        point_tag_to_id[point['tag']] = index

    data['start'] = [point_tag_to_id[x] for x in data['p1_tag']]
    data['end'] = [point_tag_to_id[x] for x in data['p2_tag']]

    return data


def get_adj_mat(points, edges):
    ret = [[-1 for x in range(len(points))] for y in range(len(points))]

    for index, edge in edges.iterrows():
        ret[edge['start']][edge['end']] = index
        ret[edge['end']][edge['start']] = index

    return ret


def get_adj_list(points, edges):
    ret = [[] for x in range(len(points))]

    for index, edge in edges.iterrows():
        ret[edge['end']].append([edge['start'], index])
        ret[edge['start']].append([edge['end'], index])

    return ret
