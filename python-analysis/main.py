import cv2


def getFeaturePoints(path):
    img = cv2.imread(path + ".jpeg")
    # gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    # Applying SIFT detector
    sift = cv2.xfeatures2d.SIFT_create()
    kp = sift.detect(img, None)

    img = cv2.drawKeypoints(img, kp, img, flags=cv2.DRAW_MATCHES_FLAGS_DRAW_RICH_KEYPOINTS)
    cv2.imwrite(path + "-fp.jpeg", img)


getFeaturePoints("./images/clough/1/1")
getFeaturePoints("./images/clough/1/2")
