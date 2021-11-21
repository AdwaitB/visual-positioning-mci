NPOINTS = 10

SEED = 1

DEBUG_LEVEL = 0

ORIGINAL_DATA = True

# Used mainly as tags, repurposed as colors to plot
POINT_DEFAULT = 'lightblue'
POINT_SEMI_1 = 'crimson'
POINT_SEMI_2 = 'fuchsia'
POINT_VIEW = 'black'

EDGE_DEFAULT = 'lightblue'
EDGE_SPLIT = 'dodgerblue'
EDGE_VIEW = 'black'

ORIGIN_COLOR = 'lime'
VIEWPOINT_COLOR = 'orange'
VIEWPOINT_EDGE_COLOR = 'orange'

AXIS_POINT_COLOR = 'lightgreen'
AXIS_EDGE_COLOR = 'lightgreen'

FOV = 70

if ORIGINAL_DATA:
    RADIUS = 0.0015
    # ORIGIN = [33.77435829374495, -84.397344643052] # Tech green
    ORIGIN = [33.7754096889298, -84.39580062968061] # Bus Stop
    ORIGIN.reverse()

    DIFF = 0.001
else:
    RADIUS = 6.5
    ORIGIN = [4, 4]

    DIFF = 0.1


DIRECTION = [0, -1]
