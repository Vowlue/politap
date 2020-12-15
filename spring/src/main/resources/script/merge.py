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
    state = jobInfo[5]
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
        count = 0
        for districts in district_ids:
            cursor.execute("select precinct_id from district_precinct where district_id="+str(districts))
            precinct_ids = [x[0] for x in cursor.fetchall()]
            for precincts in precinct_ids:
                df.at[precincts,dfname] = count
            count+=1
        dfindex+=1

    cursor.close()

    df1 = df.dissolve(by='District1')
    df2 = df.dissolve(by='District2')
    df3 = df.dissolve(by='District3')
    df4 = df.dissolve(by='District4')
    
    df1.to_file(PATH + '\..\json\generatedDistrictings\\' + str(jobid) + 'average.geojson', driver='GeoJSON')
    while (not path.exists(PATH + '\..\json\generatedDistrictings\\' + str(jobid) + 'average.geojson')):
        pass
    df2.to_file(PATH + '\..\json\generatedDistrictings\\' + str(jobid) + 'extreme.geojson', driver='GeoJSON')
    while (not path.exists(PATH + '\..\json\generatedDistrictings\\' + str(jobid) + 'extreme.geojson')):
        pass
    df3.to_file(PATH + '\..\json\generatedDistrictings\\' + str(jobid) + 'random1.geojson', driver='GeoJSON')
    while (not path.exists(PATH + '\..\json\generatedDistrictings\\' + str(jobid) + 'random1.geojson')):
        pass
    df4.to_file(PATH + '\..\json\generatedDistrictings\\' + str(jobid) + 'random2.geojson', driver='GeoJSON')   
    while (not path.exists(PATH + '\..\json\generatedDistrictings\\' + str(jobid) + 'random2.geojson')):
        pass        
        
if __name__ == '__main__':
    main(sys.argv[1:])