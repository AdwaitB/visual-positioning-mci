import time
import cv2


def match_feature_points(d1, d2, mode="flann"):
    if mode == "flann":
        start = time.time()

        flann = cv2.FlannBasedMatcher(dict(algorithm=0, trees=5), dict(checks=50))

        end = time.time()
        print("Flann Matching took ", end - start, " seconds.")
        return flann.knnMatch(d1, d2, k=2)
    else:
        start = time.time()
        bf = cv2.BFMatcher(cv2.NORM_L1, crossCheck=True)

        matches = bf.match(d1, d2)
        matches = sorted(matches, key=lambda x: x.distance)

        end = time.time()
        print("Matching took ", end - start, " seconds.")
        return matches


def lowes_ratio_test(matches):
    ret = []

    for m, n in matches:
        if m.distance < 0.7 * n.distance:
            ret.append(m)

    return ret