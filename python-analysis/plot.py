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
        plt.text(point[0], point[1], text)


def plot_edge(p1, p2, color, text=""):
    p = generate_edge_point_raw(p1, p2)
    plt.plot(p[0], p[1], linestyle="--", c=color)

    if text != "":
        plt.text((p1[0] + p2[0])/2, (p1[1] + p2[1])/2, text)


def plot_situation(points, edges, viewpoint, viewpoint_begin, viewpoint_end, axis_xp, axis_xn):
    print_util(points)
    print_util(edges)

    plt.xlim([ORIGIN[0] - RADIUS*1.1, ORIGIN[0] + RADIUS*1.1])
    plt.ylim([ORIGIN[1] - RADIUS*1.1, ORIGIN[1] + RADIUS*1.1])

    plt.rcParams["figure.autolayout"] = True

    # Plot the building edges
    for index, edge in edges.iterrows():
        [p1, p2] = generate_edge_point(points, edge['start'], edge['end'])
        plot_edge(p1, p2, edge['color'], index)

    # Plot the points
    for index, row in points.iterrows():
        plot_point([row['x'], row['y']], row['color'], index)

    # Plot the origin
    plt.scatter(ORIGIN[0], ORIGIN[1], c=ORIGIN_COLOR)

    # Plot the point of view
    plot_point(viewpoint, AXIS_POINT_COLOR)
    plot_point(viewpoint_begin, VIEWPOINT_COLOR)
    plot_point(viewpoint_end, VIEWPOINT_COLOR)
    plot_point(axis_xp, AXIS_POINT_COLOR)
    plot_point(axis_xn, AXIS_POINT_COLOR)

    plot_edge(ORIGIN, viewpoint, AXIS_EDGE_COLOR)
    plot_edge(ORIGIN, viewpoint_begin, VIEWPOINT_EDGE_COLOR)
    plot_edge(ORIGIN, viewpoint_end, VIEWPOINT_EDGE_COLOR)
    plot_edge(ORIGIN, axis_xp, AXIS_EDGE_COLOR)
    plot_edge(ORIGIN, axis_xn, AXIS_EDGE_COLOR)

    angle = np.linspace(0, 2 * math.pi, 150)

    x = RADIUS * np.cos(angle) + ORIGIN[0]
    y = RADIUS * np.sin(angle) + ORIGIN[1]

    plt.plot(x, y, linestyle="--", c=AXIS_EDGE_COLOR)

    plt.grid(True)

    fig = plt.gcf()
    fig.set_size_inches(7, 7)
    plt.show()
