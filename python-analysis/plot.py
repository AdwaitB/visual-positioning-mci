import math

import random as rd
import numpy as np
import pandas as pd

import matplotlib.pyplot as plt

from pprint import pprint

from config import *
from misc_utils import *


def generate_edge_point(points, i, j):
    return [
        [points['x'][i], points['y'][i]],
        [points['x'][j], points['y'][j]]
    ]


def generate_edge_point_raw(p1, p2):
    return [
        [p1[0], p2[0]],
        [p1[1], p2[1]]
    ]


def plot_point(point, color, text=""):
    plt.scatter(point[0], point[1], c=color)

    if text != "":
        plt.text(point[0], point[1], text, clip_on=True)


def plot_edge(p1, p2, color, text=""):
    p = generate_edge_point_raw(p1, p2)
    plt.plot(p[0], p[1], linestyle="--", c=color)

    if text != "":
        plt.text((p1[0] + p2[0])/2, (p1[1] + p2[1])/2, text, clip_on=True)


def plot_situation(origin, radius, points, edges, viewpoint, viewpoint_begin, viewpoint_end, axis_xp, axis_xn):
    if DEBUG_LEVEL >= 0:
        print_util(points)
        print_util(edges)

    plt.xlim([origin[0] - radius * 1.1, origin[0] + radius * 1.1])
    plt.ylim([origin[1] - radius * 1.1, origin[1] + radius * 1.1])

    # Plot the building edges
    for index, edge in edges.iterrows():
        [p1, p2] = generate_edge_point(points, edge['start'], edge['end'])
        plot_edge(p1, p2, edge['color'], index)

    # Plot the points
    for index, row in points.iterrows():
        plot_point([row['x'], row['y']], row['color'], index)

    # Plot the origin
    plt.scatter(origin[0], origin[1], c=ORIGIN_COLOR)

    # Plot the point of view
    plot_point(viewpoint, AXIS_POINT_COLOR)
    plot_point(viewpoint_begin, VIEWPOINT_COLOR)
    plot_point(viewpoint_end, VIEWPOINT_COLOR)
    plot_point(axis_xp, AXIS_POINT_COLOR)
    plot_point(axis_xn, AXIS_POINT_COLOR)

    plot_edge(origin, viewpoint, AXIS_EDGE_COLOR)
    plot_edge(origin, viewpoint_begin, VIEWPOINT_EDGE_COLOR)
    plot_edge(origin, viewpoint_end, VIEWPOINT_EDGE_COLOR)
    plot_edge(origin, axis_xp, AXIS_EDGE_COLOR)
    plot_edge(origin, axis_xn, AXIS_EDGE_COLOR)

    angle = np.linspace(0, 2 * math.pi, 150)

    x = radius * np.cos(angle) + origin[0]
    y = radius * np.sin(angle) + origin[1]

    plt.plot(x, y, linestyle="--", c=AXIS_EDGE_COLOR)

    plt.grid(True)
    plt.gca().set_aspect('equal', adjustable='box')
    fig = plt.gcf()
    fig.set_size_inches(7, 7)
    plt.show()
