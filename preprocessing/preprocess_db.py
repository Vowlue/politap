import csv
import json

states = ['AR','SC','VA']
table = []

precinct= []

for state in states:
    with open(state+'/'+state+'_Precinct.json') as precinctMap:
        m = json.load(precinctMap)
        for p in m['features']:
            tmp = {}
            prop = p['properties']
            tmp['geoid'] = prop['GEOID']
            tmp['state'] = state
            tmp['name'] = prop['NAME']
            tmp['county'] = prop['COUNTY']
            del prop['NAME']
            del prop['COUNTY']
            '''
            tmp['GEOID'] = prop['GEOID']
            tmp["Total"] = prop['Total']
            tmp["Hispanic or Latino"] = prop["Hispanic or Latino"]
            tmp["White"] = prop["White"]
            tmp["Black or African American"] = prop["Black or African American"]
            tmp["American Indian and Alaska Native"] = prop["American Indian and Alaska Native"]
            tmp["Asian"] = prop["Asian"]
            tmp["Native Hawaiian and Other Pacific Islander"] = prop["Native Hawaiian and Other Pacific Islander"]
            tmp["Some Other Race"] = prop["Some Other Race"]
            '''
            table.append(prop)
            precinct.append(tmp)

demos = table[0].keys()
pinfo = precinct[0].keys()

with open('db_csv/precinctdemo.csv','w') as csvfile:
    writer = csv.DictWriter(csvfile,fieldnames = demos)
    writer.writeheader()
    writer.writerows(table)
                    

with open('db_csv/precinct.csv','w') as csvfile:
    writer = csv.DictWriter(csvfile,fieldnames = pinfo)
    writer.writeheader()
    writer.writerows(precinct)             
                    
