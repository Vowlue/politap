# -*- coding: utf-8 -*-
"""
Created on Tue Dec 15 05:24:45 2020

@author: ricky
"""

import mysql.connector
import json
import geopandas as gpd
import os
from os import path

PATH = os.path.dirname(os.path.abspath(__file__))

state_translation = {'AR':'ARKANSAS','SC':'SOUTH_CAROLINA','VA':'VIRGINIA'}

def main (argv):
    jobid = int(argv[0])

    mydb = mysql.connector.connect(
    host="mysql3.cs.stonybrook.edu",
    user="jaguars",
    password="changeit",
    database="jaguars"
    )
    cursor = mydb.cursor()
    cursor.execute("select * from job_results where jobid="+str(jobid))
    jobResults = cursor.fetchone()
    cursor.execute("select * from job_info where id="+str(jobid))
    jobInfo = cursor.fetchone()
    state = jobInfo[6]
    compactness = jobInfo[1]
    demographics = jobInfo[2].split(",")
    populationvariance = jobInfo[5]
    maps = [jobResults[1],jobResults[2],jobResults[4],jobResults[5]]
    
    df = gpd.read_file(PATH + '\..\json\\' + state + '_Precinct.json')
    with open(PATH + '\..\json\\' + str(state)+'_Neighbor.json') as f:
        neighbor = json.load(f)
    df["adjacentPrecincts"]= [list(neighbor[y]["neighbor"].keys()) for x,y in df[["GEOID"]].itertuples()]
    
    df = df.rename(columns={"NAME":"precinct","GEOID":"precinctID","COUNTY":"county","Total":"population","Total VAP":"votingAgePopulation"})
    precinctjson = df[["precinct","precinctID","county","population","votingAgePopulation","adjacentPrecincts","geometry"]]
    
    df1 = gpd.read_file(PATH + '\..\json\\generatedDistrictings\\' + str(jobid)+"average.geojson")
    df2 = gpd.read_file(PATH + '\..\json\\generatedDistrictings\\' + str(jobid)+"extreme.geojson")
    df3 = gpd.read_file(PATH + '\..\json\\generatedDistrictings\\' + str(jobid)+"random1.geojson")
    df4 = gpd.read_file(PATH + '\..\json\\generatedDistrictings\\' + str(jobid)+"random2.geojson")
    
    dataframes = []
    dataframes.append(df1)
    dataframes.append(df2)
    dataframes.append(df3)
    dataframes.append(df4)
    count = 0
    for data in dataframes:
        data["adjacentDistricts"] = None
        data["precinctsInfo"] = None
        for index, row in data.iterrows():
            neighbors = data[~data.geometry.disjoint(row.geometry)].index.tolist()
            neighbors = [ str(name) for name in neighbors if index != name ]
            data.at[index, "adjacentDistricts"] = neighbors
        for x,y in data[["geometry"]].itertuples():
            cursor.execute("select * from district_precinct where district_id="+str(x))
            precinctinfo = []
            for precinct in cursor.fetchall():
                precinctinfo.append({"precinctID":str(precinct[1]),"minorityPopulation":precinct[2],"minorityVotingAgePopulation":precinct[3]})
            data.at[x, "precinctsInfo"] = precinctinfo
        if count == 0:
            df1 = data
        if count == 1:
            df2 = data
        if count == 2:
            df3 = data
        if count == 3:
            df4 = data
    
    print(precinctjson.__geo_interface__)
    ret = {"states": [{"stateName":state_translation.get(state),
    "stateID":state,
    "precinctsGeoJson": precinctjson.__geo_interface__,
    "averageDistricting":"1",
    "extremeDistricting":"2",
    "random1Districting":"3",
    "random2Districting":"4",
    "districtings": [ {
    "districtingID":"1",
    "constraints":{"compactnessLimit":compactness, "populationDifferenceLimit":populationvariance,"minorityGroups":demographics},
    "congressionalDistrictsGeoJSON": df1.__geo_interface__
    },
    {
    "districtingID":"2",
    "constraints":{"compactnessLimit":compactness, "populationDifferenceLimit":populationvariance,"minorityGroups":demographics},
    "congressionalDistrictsGeoJSON": df2.__geo_interface__
    },
    {
    "districtingID":"3",
    "constraints":{"compactnessLimit":compactness, "populationDifferenceLimit":populationvariance,"minorityGroups":demographics},
    "congressionalDistrictsGeoJSON": df3.__geo_interface__
    },
    {
    "districtingID":"4",
    "constraints":{"compactnessLimit":compactness, "populationDifferenceLimit":populationvariance,"minorityGroups":demographics},
    "congressionalDistrictsGeoJSON": df4.__geo_interface__
    }
    ]
    }
    ]}
    with open(PATH + '\..\json\\generatedDistrictings\\' + str(jobid) + "results.json", 'w') as fp:
        json.dump(ret, fp)

if __name__ == '__main__':
    main([2])