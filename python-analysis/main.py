import cv2

img = cv2.imread('example.jpeg')

cv2.imwrite('out.jpeg', img)