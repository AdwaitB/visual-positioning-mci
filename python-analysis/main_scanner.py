import math

import numpy as np
import pandas as pd

import matplotlib.pyplot as plt
import matplotlib.collections as mc

from pprint import pprint

from plot import *
from building import *
from config import *
from generator import *
from dbutils import *


def print_util(data, title=""):
    if title != "":
        print(title)
    print(data)
    print()


base_angle = get_angle(DIRECTION)

viewpoint = add_vector_to_point(ORIGIN, DIRECTION, RADIUS)

viewpoint_begin = rotate_point(ORIGIN, viewpoint, FOV/2)
viewpoint_end = rotate_point(ORIGIN, viewpoint, -FOV/2)

axis_xp = rotate_point(ORIGIN, viewpoint, -90)
axis_xn = rotate_point(ORIGIN, viewpoint, 90)

print("base_angle ", base_angle)
print("base_angle ", base_angle)
print("base_angle ", base_angle)
print("base_angle ", base_angle)


points = generate_data(NPOINTS)
plt.scatter(points['x'], points['y'], c='r')
plt.grid(True)
print(points)

edge_ids = get_edge_ids()
pprint(edge_ids)

edge_ids_mat = get_adj_mat(points, edge_ids)
pprint(edge_ids_mat)

edge_ids_list = get_adj_list(points, edge_ids)
pprint(edge_ids_list)

id_to_build_map, buildings = get_building_set(points, edge_ids_list)

tag_points(points)
print(points)

plot_situation(points, edge_ids, viewpoint, viewpoint_begin, viewpoint_end, axis_xp, axis_xn)



print(split_edges_building(1))
print()
print(split_edges_building(2))
print()

