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
