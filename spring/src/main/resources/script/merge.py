# -*- coding: utf-8 -*-
"""
Created on Mon Dec 14 16:59:04 2020

@author: ricky
"""

import mysql.connector
import geopandas as gpd
import os
import sys
from os import path

PATH = os.path.dirname(os.path.abspath(__file__))

def main(argv):
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
    maps = [jobResults[1],jobResults[2],jobResults[4],jobResults[5]]

    df = gpd.read_file(PATH + '\..\json\\' + state + '_Precinct.json')
    df = df[["GEOID","geometry"]]
    df = df.set_index("GEOID")
    df["District1"] = None
    df["District2"] = None
    df["District3"] = None
    df["District4"] = None

    dfid = ["District1","District2","District3","District4"]
    dfindex = 0
    for mapindex in maps:
        cursor.execute("select id from districts where districting_id="+str(mapindex))
        district_ids = [x[0] for x in cursor.fetchall()]
        dfname = dfid[dfindex]
        for districts in district_ids:
            cursor.execute("select precinctid from district_precinct where districtid="+str(districts))
            precinct_ids = [x[0] for x in cursor.fetchall()]
            for precincts in precinct_ids:
                df.at[precincts,dfname] = districts
        dfindex+=1

    df1 = df.dissolve(by='District1')
    df1 = df1.drop(columns=["District2","District3","District4"])
    df2 = df.dissolve(by='District2')
    df2 = df2.drop(columns=["District1","District3","District4"])
    df3 = df.dissolve(by='District3')
    df3 = df3.drop(columns=["District2","District1","District4"])
    df4 = df.dissolve(by='District4')
    df4 = df4.drop(columns=["District2","District3","District1"])
    
    dataframes = []
    dataframes.append(df1)
    dataframes.append(df2)
    dataframes.append(df3)
    dataframes.append(df4)
    count = 0
    for data in dataframes:
        data["population"] = None
        data["minorityPopulation"] = None
        data["votingAgePopulation"] = None
        data["minorityVotingAgePopulation"] = None
        data["differentCounties"] = None
        #data["adjacentDistricts"] = None
        #data["precinctsInfo"] = None
        #for index, row in data.iterrows():
        #    neighbors = data[~data.geometry.disjoint(row.geometry)].index.tolist()
        #    neighbors = [ str(name) for name in neighbors if index != name ]
        #    data.at[index, "adjacentDistricts"] = neighbors
        for x,y in data[["geometry"]].itertuples():
            cursor.execute("select * from districts where id="+str(x))
            districtinfo = cursor.fetchone()
            data.at[x, "differentCounties"] = districtinfo[1]
            data.at[x, "minorityPopulation"] = districtinfo[2]
            data.at[x, "minorityVotingAgePopulation"] = districtinfo[3]
            data.at[x, "population"] = districtinfo[4]
            data.at[x, "votingAgePopulation"] = districtinfo[5]
            #cursor.execute("select * from district_precinct where district_id="+str(districts))
            #precinctinfo = []
            #for precinct in cursor.fetchall():
            #    precinctinfo.append({"precinctID":str(precinct[1]),"minorityPopulation":precinct[2],"minorityVotingAgePopulation":precinct[3]})
            #data.at[x, "precinctsInfo"] = precinctinfo
        if count == 0:
            data.to_file(PATH + '\..\json\generatedDistrictings\\' + str(jobid) + 'average.geojson', driver='GeoJSON')
        if count == 1:
            data.to_file(PATH + '\..\json\generatedDistrictings\\' + str(jobid) + 'extreme.geojson', driver='GeoJSON')
        if count == 2:
            data.to_file(PATH + '\..\json\generatedDistrictings\\' + str(jobid) + 'random1.geojson', driver='GeoJSON')
        if count == 3:
            data.to_file(PATH + '\..\json\generatedDistrictings\\' + str(jobid) + 'random2.geojson', driver='GeoJSON')  
        count+=1   
        
if __name__ == '__main__':
    main(sys.argv[1:])