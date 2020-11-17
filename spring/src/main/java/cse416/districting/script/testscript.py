import time
import random

time.sleep(5)
f = open("File" + str(random.randint(1,500)) + ".txt", "a")
f.write("yolo")
f.close()