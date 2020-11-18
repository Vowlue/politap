import random
import time
import sys
import os
import geopandas as gpd

PATH = os.path.dirname(os.path.abspath(__file__))

def main(argv):
    df = gpd.read_file(PATH + '\..\json\AR_Precinct.json')
    df = df.dropna() 
    df = df[["COUNTY","geometry"]]
    df = df.dissolve(by='COUNTY')
    df.to_file(PATH + "\..\json\AR_new.geojson", driver='GeoJSON')
    time.sleep(5)
    print("AR_new.geojson")
    
if __name__ == '__main__':
    main(sys.argv[1:])
