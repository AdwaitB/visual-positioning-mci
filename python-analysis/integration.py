from scanner import *

if ORIGINAL_DATA:
    radius = 0.0015
    # ORIGIN = [33.77435829374495, -84.397344643052] # Tech green
    origin = [33.7754096889298, -84.39580062968061] # Bus Stop
    origin.reverse()

    DIFF = 0.001
else:
    radius = 6.5
    origin = [4, 4]

    DIFF = 0.1


direction = [0, -1]


def main():
    edges = tag_edges_by_view(origin, direction, radius)

    print_util(edges, "edges final")


if __name__ == '__main__':
    main()