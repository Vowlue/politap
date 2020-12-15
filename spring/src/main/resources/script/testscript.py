import random
import sys
import os
import geopandas as gpd
from collections import deque
import json

PATH = os.path.dirname(os.path.abspath(__file__))
state_translation = {'ARKANSAS':'\AR','SOUTH_CAROLINA':'\SC','VIRGINIA':'\VA'}
districts_translation = {'ARKANSAS':4,'SOUTH_CAROLINA':7,'VIRGINIA':11}

def main(argv):
    state = state_translation.get(argv[0])
    districts = districts_translation.get(argv[0])
    runs = int(argv[1])
    jobid = int(argv[2])

    df = gpd.read_file(PATH + '\..\json' + state + '_Precinct_New.geojson')

    districtings = []
    for i in range(runs):
        districting = []
        for j in range(districts):
            districting.append([])
    
        df['DISTRICTID'] = None
        RNG = random.sample(range(0,len(df)), districts)
        x = 0
        q = deque([])
        for n in RNG:
            df.at[n,'DISTRICTID'] = x
            q.append(df.at[n,'GEOID'])
            x += 1
            
        df1 = df.set_index('GEOID')
        while len(q) != 0:
            geoid = q.popleft()
            districtid = df1.loc[geoid,'DISTRICTID']
            districting[districtid].append(geoid)
            for neighbor in df1.loc[geoid,'NEIGHBORS'].replace(" ","").split(","):
                if df1.loc[neighbor,'DISTRICTID'] == None:
                    df1.loc[neighbor,'DISTRICTID'] = districtid
                    q.append(neighbor)
        districtings.append(districting)
    
    dic = {}
    dic["plans"] = districtings
    
    with open(PATH + '\..\json\generatedDistrictings' + str(state) + str(jobid) + '.json', "w") as outfile:
        json.dump(dic,outfile)
        outfile.close()
    
    print('json\generatedDistrictings' + str(state) + str(jobid) + '.json')

if __name__ == '__main__':
    main(sys.argv[1:])
