import cv2

BUILDINGS   = ['CLOUGH']
ANGLES      = [1,2,3,4,5,6,7]
TIME_OF_DAY = [1,2]

PREFIX_FOLDER = "img/"

ASPECT_RATIO = 4/3

def generateImgPaths():
    ans = []
    
    for building in BUILDINGS:
        for angle in ANGLES:
            for time in TIME_OF_DAY:
                ans.append(building + "-" + str(angle) + "-" + str(time))
    
    return ans

def generateFeaturePointsToFile(path):
    img = cv2.imread(path + ".jpg")
    size = 1024
    resized = cv2.resize(img, (int(ASPECT_RATIO*size), size), interpolation = cv2.INTER_AREA)

    # gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    # Applying SIFT detector
    sift = cv2.xfeatures2d.SIFT_create()
    kp = sift.detect(resized, None)

    resized = cv2.drawKeypoints(resized, kp, resized, flags=cv2.DRAW_MATCHES_FLAGS_DRAW_RICH_KEYPOINTS)
    cv2.imwrite(path + "-fp.jpg", resized)


def main():

    IMG_PATHS = generateImgPaths()

    IMG_PATHS = [PREFIX_FOLDER + x for x in IMG_PATHS]

    for image_path in IMG_PATHS:
        generateFeaturePointsToFile(image_path)

if __name__ == "__main__":
    main()
