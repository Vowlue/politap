import random
import time
import sys
import os
import geopandas as gpd
from collections import deque

PATH = os.path.dirname(os.path.abspath(__file__))
state_translation = {"ARKANSAS":"\AR","SOUTH_CAROLINA":"\SC","VIRGINIA":"\VA"}

def main(argv):
    state = state_translation.get(argv[0])
    jobid = argv[1]
    districts = argv[2]

    df = gpd.read_file(PATH + '\..\json' + state + '_Precinct_New.geojson')
    df["DISTRICTID"] = None
    RNG = random.sample(range(0,len(df)), districts)
    x = 0
    q = deque([])
    for n in RNG:
        df.at[n,"DISTRICTID"] = x
        q.append(df.at[n,"GEOID"])
        x += 1
        
    df = df.set_index("GEOID")
    while len(q) != 0:
        geoid = q.popleft()
        districtid = df.loc[geoid,"DISTRICTID"]
        for neighbor in df.loc[geoid,"NEIGHBORS"].replace(" ","").split(","):
            if df.loc[neighbor,"DISTRICTID"] == None:
                df.loc[neighbor,"DISTRICTID"] = districtid
                q.append(neighbor)

    df = df.dissolve(by='DISTRICTID')

    df.to_file(PATH + "\..\json\generatedDistrictings\\" + str(jobid) + ".geojson", driver='GeoJSON')
    time.sleep(5)
    print(str(jobid) + ".geojson")
    
if __name__ == '__main__':
    main(sys.argv[1:])
