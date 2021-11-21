from scanner import *


if ORIGINAL_DATA:
    radius = 0.001

    data = {
        "TG": [[33.77435829374495, -84.397344643052], [1, 0]],
        "BusStop": [[33.7754096889298, -84.39580062968061], [0, -1]],
        "Hive1": [[33.77567129853454, -84.39715893627394], [1, 1]],
        "Hive2": [[33.77567129853454, -84.39715893627394], [-1, -1]],
        "Pettit": [[33.77637681793485, -84.39690871277799], [1, 1]],
        "Klaus1": [[33.77692237952651, -84.39637874707857], [1, 1]],
        "Klaus2": [[33.77692237952651, -84.39637874707857], [-1, 1]],
        "IndustrialDesign": [[33.77570781753953, -84.39607218474292], [1, 1]]
    }
    origin, direction = data["IndustrialDesign"]
    origin.reverse()
    DIFF = 0.001
else:
    radius = 6.5
    origin = [4, 4]

    DIFF = 0.1
    direction = [1, 1]


def main():
    edges = tag_edges_by_view(origin, direction, radius)
    print_util(edges, "edges final")


if __name__ == '__main__':
    main()