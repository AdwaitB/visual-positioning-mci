import random as rd
import pandas as pd

from config import *

rd.seed(SEED)


def generate_data(count):
    x = []
    y = []

    for i in range(0, count):
        x.append(rd.randrange(1000)/100.0)
        y.append(rd.randrange(1000)/100.0)

    return pd.DataFrame({'x': x, 'y': y, 'color': [EDGE_DEFAULT] * len(x)})


def get_edges():
    if SEED == 1:
        ret = pd.DataFrame({
            'start': [0, 0, 4, 4, 6, 6, 2, 2],
            'end': [9, 8, 5, 1, 5, 1, 3, 7],
        })

        ret['color'] = [EDGE_DEFAULT] * len(ret)
        return ret
