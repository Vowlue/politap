import random
import time
import sys
import os
import geopandas as gpd
from collections import deque

PATH = os.path.dirname(os.path.abspath(__file__))
state_translation = {'ARKANSAS':'\AR','SOUTH_CAROLINA':'\SC','VIRGINIA':'\VA'}
districts_translation = {'ARKANSAS':4,'SOUTH_CAROLINA':7,'VIRGINIA':11}

def main(argv):
    state = state_translation.get(argv[0])
    districts = districts_translation.get(argv[0])
    jobid = argv[1]

    df = gpd.read_file(PATH + '\..\json' + state + '_Precinct_New.geojson')

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
        for neighbor in df1.loc[geoid,'NEIGHBORS'].replace(" ","").split(","):
            if df1.loc[neighbor,'DISTRICTID'] == None:
                df1.loc[neighbor,'DISTRICTID'] = districtid
                q.append(neighbor)

    df['DISTRICTID2'] = None
    RNG = random.sample(range(0,len(df)), districts)
    x = 0
    q = deque([])
    for n in RNG:
        df.at[n,'DISTRICTID2'] = x
        q.append(df.at[n,'GEOID'])
        x += 1
        
    df2 = df.set_index('GEOID')
    while len(q) != 0:
        geoid = q.popleft()
        districtid = df2.loc[geoid,'DISTRICTID2']
        for neighbor in df2.loc[geoid,'NEIGHBORS'].replace(" ","").split(","):
            if df2.loc[neighbor,'DISTRICTID2'] == None:
                df2.loc[neighbor,'DISTRICTID2'] = districtid
                q.append(neighbor)

    df1 = df1.dissolve(by='DISTRICTID')
    df1.to_file(PATH + '\..\json\generatedDistrictings' + str(state) + str(jobid) + '.geojson', driver='GeoJSON')
    df2 = df2.dissolve(by='DISTRICTID2')
    df2.to_file(PATH + '\..\json\generatedDistrictings' + str(state) + str(jobid) + '-2.geojson', driver='GeoJSON')

    time.sleep(2)
    print(str(state)[1:] + str(jobid))
    
if __name__ == '__main__':
    main(sys.argv[1:])
